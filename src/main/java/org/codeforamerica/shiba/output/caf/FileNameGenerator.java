package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FileNameGenerator {
    public static final Map<String, Set<String>> LETTER_TO_PROGRAMS = Map.of(
            "E", Set.of("EA"),
            "K", Set.of("CASH", "GRH"),
            "F", Set.of("SNAP"),
            "C", Set.of("CCAP")
    );
    private final CountyMap<MnitCountyInformation> countyMap;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public FileNameGenerator(CountyMap<MnitCountyInformation> countyMap) {
        this.countyMap = countyMap;
    }

    public String generateFileName(Application application) {
        List<String> programsList = application.getApplicationData().getPagesData().getPage("choosePrograms").get("programs").getValue();
        final StringBuilder programs = new StringBuilder();
        List.of("E", "K", "F", "C").forEach(letter -> {
                    if (programsList.stream()
                            .anyMatch(program -> LETTER_TO_PROGRAMS.get(letter)
                                    .contains(program))) {
                        programs.append(letter);
                    }
                }
        );

        return countyMap.get(application.getCounty()).getDhsProviderId() + "_" +
                "MNB_" +
                DateTimeFormatter.ofPattern("yyyyMMdd").format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                DateTimeFormatter.ofPattern("HHmmss").format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                application.getId() + "_" +
                programs.toString();
    }

}
