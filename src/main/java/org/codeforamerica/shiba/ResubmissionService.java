package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResubmissionService {

  private final ApplicationRepository applicationRepository;
  private final EmailClient emailClient;
  private final CountyMap<CountyRoutingDestination> countyMap;
  private final PdfGenerator pdfGenerator;
  private final RoutingDecisionService routingDecisionService;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public ResubmissionService(ApplicationRepository applicationRepository,
      EmailClient emailClient, CountyMap<CountyRoutingDestination> countyMap,
      PdfGenerator pdfGenerator,
      RoutingDecisionService routingDecisionService) {
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.countyMap = countyMap;
    this.pdfGenerator = pdfGenerator;
    this.routingDecisionService = routingDecisionService;
  }

  @Scheduled(fixedDelayString = "${resubmission.interval.milliseconds}")
  @SchedulerLock(name = "resubmissionTask", lockAtMostFor = "30m")
  public void resubmitFailedApplications() {
    log.info("Checking for applications that failed to send");
    Map<Document, List<String>> documentsToIds =
        applicationRepository.getApplicationIdsToResubmit();

    if (documentsToIds.values().stream().allMatch(List::isEmpty)) {
      log.info("There are no applications to resubmit");
      return;
    }

    documentsToIds.forEach((document, applicationIds) -> applicationIds.forEach(id -> {
      MDC.put("applicationId", id);
      log.info("Resubmitting " + document.name() + "(s) for application id " + id);
      Application application = applicationRepository.find(id);
      List<RoutingDestination> routingDestinations = routingDecisionService.getRoutingDestinations(
          application.getApplicationData(), document);
      List<String> recipientEmails = new ArrayList<>();

      routingDestinations.forEach(rd -> {
        recipientEmails.add(rd.getEmail());
      });

      log.info("Attempting to resubmit %s(s) for application id %s to emails %s".formatted(
          document.name(), id, recipientEmails));
      try {
        if (document.equals(UPLOADED_DOC)) {
          resubmitUploadedDocumentsForApplication(document, application, recipientEmails);
        } else {
          var applicationFile = pdfGenerator.generate(application, document, CASEWORKER);
          recipientEmails.forEach(
              eml -> emailClient.resubmitFailedEmail(eml, document, applicationFile, application));
        }
        applicationRepository.updateStatus(id, document, DELIVERED);
        log.info("Resubmitted %s(s) for application id %s".formatted(document.name(), id));
      } catch (Exception e) {
        log.error("Failed to resubmit application %s via email".formatted(id), e);
        applicationRepository.updateStatus(id, document, RESUBMISSION_FAILED);
      }
    }));
    MDC.clear();
  }

  private void resubmitUploadedDocumentsForApplication(Document document, Application application,
      List<String> recipientEmails) {
    var coverPage = pdfGenerator.generateCoverPageForUploadedDocs(application);
    var uploadedDocs = application.getApplicationData().getUploadedDocs();
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument uploadedDocument = uploadedDocs.get(i);
      ApplicationFile fileToSend = pdfGenerator
          .generateForUploadedDocument(uploadedDocument, i, application, coverPage);
      var esbFilename = fileToSend.getFileName();
      var originalFilename = uploadedDocument.getFilename();

      log.info("Resubmitting uploaded doc: %s original filename: %s"
          .formatted(esbFilename, originalFilename));
      recipientEmails.forEach(
          eml -> emailClient.resubmitFailedEmail(eml, document, fileToSend, application));
      log.info("Finished resubmitting document %s".formatted(esbFilename));
    }
  }
}
