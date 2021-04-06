package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public String generatePdfFileName(Application application, Document document) {
        StringBuilder programs = programs(application);

        return getSharedApplicationPrefix(application) +
                programs.toString() + "_" +
                document.toString();
    }

    public String generateUploadedDocumentName(Application application, int index, String filename) {
        int size = application.getApplicationData().getUploadedDocs().size();
        String[] fileNameParts = filename.split("\\.");
        String extension = fileNameParts.length > 1 ? fileNameParts[fileNameParts.length - 1] : "";
        index = index + 1;
        return getSharedApplicationPrefix(application) +
                "doc" + index + "of" +
                size + "." +
                extension;
    }

    public String generateXmlFileName(Application application) {
        StringBuilder programs = programs(application);

        return getSharedApplicationPrefix(application) +
                programs.toString();
    }

    @NotNull
    private String getSharedApplicationPrefix(Application application) {
        return countyMap.get(application.getCounty()).getDhsProviderId() + "_" +
                "MNB_" +
                DateTimeFormatter.ofPattern("yyyyMMdd").format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                DateTimeFormatter.ofPattern("HHmmss").format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago"))) + "_" +
                application.getId() + "_";
    }

    private StringBuilder programs(Application application) {
        Set<String> programsList = programList(application);
        final StringBuilder programs = new StringBuilder();
        List.of("E", "K", "F", "C").forEach(letter -> {
                    if (programsList.stream()
                            .anyMatch(program -> LETTER_TO_PROGRAMS.get(letter)
                                    .contains(program))) {
                        programs.append(letter);
                    }
                }
        );

        return programs;
    }


    private Set<String> programList( Application application){
        List<String> applicantProgramsList = application.getApplicationData().getPagesData().safeGetPageInputValue("choosePrograms", "programs");
        Set<String> programList = new HashSet<>(applicantProgramsList);
        boolean hasHousehold = application.getApplicationData().getSubworkflows().containsKey("household");
        if (hasHousehold) {
            List<Iteration> householdIteration = application.getApplicationData().getSubworkflows().get("household");
            householdIteration.stream().map(household -> household.getPagesData().safeGetPageInputValue("householdMemberInfo", "programs")).forEach(programList::addAll);
        }

        return programList;

    }

}
