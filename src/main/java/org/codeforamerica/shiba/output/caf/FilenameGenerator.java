package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
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
    RoutingDestination routingDestination = countyMap.get(application.getCounty());
    return generatePdfFilename(application, document, routingDestination);
  }

  public String generatePdfFilename(Application application, Document document,
      RoutingDestination routingDestination) {
    String dhsProviderId = routingDestination.getDhsProviderId();
    String prefix = getSharedApplicationPrefix(application, document, dhsProviderId);
    String programs = getProgramCodes(application);
    String pdfType = document.toString();
    return "%s%s_%s.pdf".formatted(prefix, programs, pdfType);
  }

  public String generateUploadedDocumentName(Application application, int index, String extension) {
    RoutingDestination routingDestination = countyMap.get(application.getCounty());
    return generateUploadedDocumentName(application, index, extension, routingDestination);
  }

  public String generateUploadedDocumentName(Application application, int index, String extension,
      RoutingDestination routingDestination) {
    int size = application.getApplicationData().getUploadedDocs().size();
    return generateUploadedDocumentName(application, index, extension, routingDestination, size);
  }

  public String generateUploadedDocumentName(Application application, int index, String extension,
      RoutingDestination routingDestination, int size) {
    index = index + 1;
    String dhsProviderId = routingDestination.getDhsProviderId();
    String prefix = getSharedApplicationPrefix(application, UPLOADED_DOC,
        dhsProviderId);
    return "%sdoc%dof%d.%s".formatted(prefix, index, size, extension);
  }

  public String generateXmlFilename(Application application) {
    String dhsProviderId = countyMap.get(application.getCounty()).getDhsProviderId();
    String prefix = getSharedApplicationPrefix(application, CAF,
        dhsProviderId);
    String programs = getProgramCodes(application);
    return "%s%s.xml".formatted(prefix, programs);
  }

  @NotNull
  private String getSharedApplicationPrefix(Application application, Document document,
      String dhsProviderId) {
    boolean isHennepinUploadedDoc =
        document == UPLOADED_DOC && application.getCounty() == County.Hennepin;
    String fileSource = isHennepinUploadedDoc ? "DOC" : "MNB";

    ZonedDateTime completedAtCentralTime =
        application.getCompletedAt().withZoneSameInstant(ZoneId.of("America/Chicago"));
    String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(completedAtCentralTime);
    String time = DateTimeFormatter.ofPattern("HHmmss").format(completedAtCentralTime);
    String id = application.getId();
    return "%s_%s_%s_%s_%s_".formatted(dhsProviderId, fileSource, date, time, id);
  }

  private String getProgramCodes(Application application) {
    Set<String> programSet = application.getApplicationData()
        .getApplicantAndHouseholdMemberPrograms();
    return Stream.of("E", "K", "F", "C")
        .filter(letter -> programSet.stream()
            .anyMatch(program -> LETTER_TO_PROGRAMS.get(letter).contains(program)))
        .collect(Collectors.joining());
  }

}
