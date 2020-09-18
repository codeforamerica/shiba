package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MnitCountyInformation;
import org.codeforamerica.shiba.metrics.Metrics;
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
import java.util.Set;

@Component
public class ApplicationFactory {
    private final Clock clock;
    private final Map<String, County> countyZipCode;
    private final Map<County, MnitCountyInformation> countyFolderIdMapping;
    public static final Map<String, Set<String>> LETTER_TO_PROGRAMS = Map.of(
            "E", Set.of("EA"),
            "K", Set.of("CASH", "GRH"),
            "F", Set.of("SNAP"),
            "C", Set.of("CCAP")
    );

    public ApplicationFactory(Clock clock,
                              Map<String, County> countyZipCode,
                              Map<County, MnitCountyInformation> countyFolderIdMapping) {
        this.clock = clock;
        this.countyZipCode = countyZipCode;
        this.countyFolderIdMapping = countyFolderIdMapping;
    }

    public Application newApplication(String id, ApplicationData applicationData, Metrics metrics) {
        ApplicationData copy = new ApplicationData();
        copy.setPagesData(applicationData.getPagesData());
        copy.setSubworkflows(applicationData.getSubworkflows());
        copy.setIncompleteIterations(applicationData.getIncompleteIterations());
        String zipCode = copy.getPagesData().get("homeAddress").get("zipCode").getValue().get(0);
        County county = countyZipCode.getOrDefault(zipCode, County.OTHER);
        ZonedDateTime completedAt = ZonedDateTime.now(clock);
        String fileName = createFileName(id, applicationData, county, completedAt);

        return Application.builder()
                .id(id)
                .completedAt(completedAt)
                .applicationData(copy)
                .county(county)
                .fileName(fileName)
                .timeToComplete(Duration.between(metrics.getStartTime(), completedAt))
                .build();
    }

    public Application reconstitueApplication(String id,
                                              ZonedDateTime completedAt,
                                              ApplicationData applicationData,
                                              County county,
                                              Duration timeToComplete,
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

        return countyFolderIdMapping.get(county).getDhsProviderId() + "_" +
                "MNB_" +
                DateTimeFormatter.ofPattern("yyyyMMdd").format(completedAt.withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                DateTimeFormatter.ofPattern("HHmmss").format(completedAt.withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                id + "_" +
                programs.toString();
    }
}
