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
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FilenameGenerator {

  public static final Map<String, Set<String>> LETTER_TO_PROGRAMS = Map.of(
      "E", Set.of("EA"),
      "K", Set.of("CASH", "GRH"),
      "F", Set.of("SNAP"),
      "C", Set.of("CCAP")
  );
  private final ServicingAgencyMap<CountyRoutingDestination> countyMap;
  private final SnapExpeditedEligibilityDecider decider;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public FilenameGenerator(ServicingAgencyMap<CountyRoutingDestination> countyMap, SnapExpeditedEligibilityDecider decider) {
    this.countyMap = countyMap;
    this.decider = decider;
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
    String eligible = "";
    if(decider.decide(application.getApplicationData()) == SnapExpeditedEligibility.ELIGIBLE) {
        eligible = "_EXPEDITED";
    }
    return "%s%s_%s%s.pdf".formatted(prefix, programs, pdfType, eligible);
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
  
  /*public String generateCombinedUploadedDocsName(Application application, String extension,
      RoutingDestination routingDestination, int index, int size) {
    String dhsProviderId = routingDestination.getDhsProviderId();
    String prefix = getSharedApplicationPrefix(application, UPLOADED_DOC,
        dhsProviderId);
    return "%sdoc%dof%d.%s".formatted(prefix, index, size, extension);
  }*/

  public String generateXmlFilename(Application application) {
    String dhsProviderId = countyMap.get(application.getCounty()).getDhsProviderId();
    String prefix = getSharedApplicationPrefix(application, CAF,
            dhsProviderId);
    String programs = getProgramCodes(application);
    String eligible = "";
    if(decider.decide(application.getApplicationData()) == SnapExpeditedEligibility.ELIGIBLE) {
      eligible = "_CAF_EXPEDITED";
    }
    return "%s%s%s.xml".formatted(prefix, programs, eligible);
  }

  @NotNull
  private String getSharedApplicationPrefix(Application application, Document document,
      String dhsProviderId) {
    boolean isHennepinUploadedDoc =
        document == UPLOADED_DOC && (application.getCounty() == County.Hennepin || application.getCounty() == County.Other);
    String fileSource = isHennepinUploadedDoc ? "DOC" : "MNB";

    ZonedDateTime completedAt = application.getCompletedAt();
    ZonedDateTime completedAtCentralTime;
    if(completedAt != null) {
        completedAtCentralTime = completedAt.withZoneSameInstant(ZoneId.of("America/Chicago"));
    }else {
    	log.info("completedAt was null for applicationId " + application.getId() + ". Creating new completedAt time.");
    	completedAtCentralTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("America/Chicago"));
    }

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
