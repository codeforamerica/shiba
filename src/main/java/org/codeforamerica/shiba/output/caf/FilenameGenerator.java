package org.codeforamerica.shiba.output.caf;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class FilenameGenerator {

  public static final Map<String, Set<String>> LETTER_TO_PROGRAMS = Map.of(
      "E", Set.of("EA"),
      "K", Set.of("CASH", "GRH"),
      "F", Set.of("SNAP"),
      "C", Set.of("CCAP")
  );
  private final CountyMap<CountyRoutingDestination> countyMap;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public FilenameGenerator(CountyMap<CountyRoutingDestination> countyMap) {
    this.countyMap = countyMap;
  }

  public String generatePdfFilename(Application application, Document document) {
    String prefix = getSharedApplicationPrefix(application);
    String programs = getProgramCodes(application);
    String pdfType = document.toString();
    String extension = "pdf";
    return ("%s%s_%s.%s").formatted(prefix, programs, pdfType, extension);
  }

  public String generateUploadedDocumentName(Application application, int index, String extension) {
    int size = application.getApplicationData().getUploadedDocs().size();
    index = index + 1;
    String prefix = getUploadedDocumentPrefix(application);
    return "%sdoc%dof%d.%s".formatted(prefix, index, size, extension);
  }

  public String generateXmlFilename(Application application) {
    String prefix = getSharedApplicationPrefix(application);
    String programs = getProgramCodes(application);
    String extension = "xml";
    return "%s%s.%s".formatted(prefix, programs, extension);
  }

  @NotNull
  private String getSharedApplicationPrefix(Application application) {
    var dhsProviderId = countyMap.get(application.getCounty()).getDhsProviderId();
    var date = DateTimeFormatter.ofPattern("yyyyMMdd")
        .format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")));
    var time = DateTimeFormatter.ofPattern("HHmmss")
        .format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")));
    var id = application.getId();
    return "%s_MNB_%s_%s_%s_".formatted(dhsProviderId, date, time, id);
  }

  @NotNull
  private String getUploadedDocumentPrefix(Application application) {
    var dhsProviderId = countyMap.get(application.getCounty()).getDhsProviderId();
    County county = application.getCounty();
    String fileSource = county == County.Hennepin ? "DOC" : "MNB";
    var date = DateTimeFormatter.ofPattern("yyyyMMdd")
        .format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")));
    var time = DateTimeFormatter.ofPattern("HHmmss")
        .format(application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago")));
    var id = application.getId();
    return "%s_%s_%s_%s_%s_".formatted(dhsProviderId, fileSource, date, time, id);
  }

  private String getProgramCodes(Application application) {
    Set<String> programSet = programSet(application);
    return Stream.of("E", "K", "F", "C")
        .filter(letter -> programSet.stream()
            .anyMatch(program -> LETTER_TO_PROGRAMS.get(letter).contains(program)))
        .collect(Collectors.joining());
  }

  private Set<String> programSet(Application application) {
    List<String> applicantProgramsList = application.getApplicationData().getPagesData()
        .safeGetPageInputValue("choosePrograms", "programs");
    Set<String> programs = new HashSet<>(applicantProgramsList);
    boolean hasHousehold = application.getApplicationData().getSubworkflows()
        .containsKey("household");
    if (hasHousehold) {
      List<Iteration> householdIteration = application.getApplicationData().getSubworkflows()
          .get("household");
      householdIteration.stream().map(household -> household.getPagesData()
          .safeGetPageInputValue("householdMemberInfo", "programs")).forEach(programs::addAll);
    }
    return programs;
  }
}
