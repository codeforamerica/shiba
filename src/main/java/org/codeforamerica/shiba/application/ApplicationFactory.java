package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.LocationClient;
import org.codeforamerica.shiba.MnitCountyInformation;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.pages.CountyMap;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.codeforamerica.shiba.County.OTHER;

@Component
public class ApplicationFactory {
    private final Clock clock;
    private final Map<String, County> countyZipCode;
    private final CountyMap<MnitCountyInformation> countyMap;
    private final LocationClient locationClient;
    private final ApplicationDataParser<Address> homeAddressParser;
    public static final Map<String, Set<String>> LETTER_TO_PROGRAMS = Map.of(
            "E", Set.of("EA"),
            "K", Set.of("CASH", "GRH"),
            "F", Set.of("SNAP"),
            "C", Set.of("CCAP")
    );

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ApplicationFactory(
            Clock clock,
            Map<String, County> countyZipCode,
            CountyMap<MnitCountyInformation> countyMap,
            LocationClient locationClient,
            ApplicationDataParser<Address> homeAddressParser) {
        this.clock = clock;
        this.countyZipCode = countyZipCode;
        this.countyMap = countyMap;
        this.locationClient = locationClient;
        this.homeAddressParser = homeAddressParser;
    }

    public Application newApplication(String id, ApplicationData applicationData, Metrics metrics) {
        ApplicationData copy = new ApplicationData();
        copy.setPagesData(applicationData.getPagesData());
        copy.setSubworkflows(applicationData.getSubworkflows());
        copy.setIncompleteIterations(applicationData.getIncompleteIterations());
        copy.setFlow(applicationData.getFlow());
        Address homeAddress = homeAddressParser.parse(applicationData);
        Optional<String> countyString = locationClient.getCounty(homeAddress);
        County county = countyString.map(County::fromString)
                .orElse(countyZipCode.getOrDefault(homeAddress.getZipcode(), OTHER));
        ZonedDateTime completedAt = ZonedDateTime.now(clock);
        String fileName = createFileName(id, applicationData, county, completedAt);

        return Application.builder()
                .id(id)
                .completedAt(completedAt)
                .applicationData(copy)
                .county(county)
                .fileName(fileName)
                .timeToComplete(Duration.between(metrics.getStartTime(), completedAt))
                .flow(applicationData.getFlow())
                .build();
    }

    public Application reconstituteApplication(String id,
                                               ZonedDateTime completedAt,
                                               ApplicationData applicationData,
                                               County county,
                                               Duration timeToComplete,
                                               FlowType flow,
                                               Sentiment sentiment,
                                               String feedback) {
        return Application.builder()
                .id(id)
                .completedAt(completedAt)
                .applicationData(applicationData)
                .county(county)
                .fileName(createFileName(id, applicationData, county, completedAt))
                .timeToComplete(timeToComplete)
                .sentiment(sentiment)
                .feedback(feedback)
                .flow(flow)
                .build();
    }

    @NotNull
    private String createFileName(String id, ApplicationData applicationData, County county, ZonedDateTime completedAt) {
        List<String> programsList = applicationData.getPagesData().getPage("choosePrograms").get("programs").getValue();
        final StringBuilder programs = new StringBuilder();
        List.of("E", "K", "F", "C").forEach(letter -> {
                    if (programsList.stream()
                            .anyMatch(program -> LETTER_TO_PROGRAMS.get(letter)
                                    .contains(program))) {
                        programs.append(letter);
                    }
                }
        );

        return countyMap.get(county).getDhsProviderId() + "_" +
                "MNB_" +
                DateTimeFormatter.ofPattern("yyyyMMdd").format(completedAt.withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                DateTimeFormatter.ofPattern("HHmmss").format(completedAt.withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                id + "_" +
                programs.toString();
    }
}
