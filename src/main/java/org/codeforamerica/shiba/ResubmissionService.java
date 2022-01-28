package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatus;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResubmissionService {

  private final ApplicationRepository applicationRepository;
  private final EmailClient emailClient;
  private final PdfGenerator pdfGenerator;
  private final RoutingDecisionService routingDecisionService;
  private final DocumentStatusRepository documentStatusRepository;
  private final PageEventPublisher pageEventPublisher;

  public ResubmissionService(ApplicationRepository applicationRepository,
      EmailClient emailClient,
      PdfGenerator pdfGenerator,
      RoutingDecisionService routingDecisionService,
      DocumentStatusRepository documentStatusRepository,
      PageEventPublisher pageEventPublisher) {
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.pdfGenerator = pdfGenerator;
    this.routingDecisionService = routingDecisionService;
    this.documentStatusRepository = documentStatusRepository;
    this.pageEventPublisher = pageEventPublisher;
  }

  @Scheduled(
      fixedDelayString = "${failed-resubmission.interval.milliseconds}", // how often to run (every 12 hours)
      initialDelayString = "${failed-resubmission.initialDelay.milliseconds:0}"
  )
  @SchedulerLock(name = "resubmissionTask", lockAtMostFor = "30m")
  public void resubmitFailedApplications() {
    log.info("Checking for applications that failed to send");
    List<DocumentStatus> applicationsToResubmit = documentStatusRepository.getDocumentStatusToResubmit();

    if (applicationsToResubmit.isEmpty()) {
      log.info("There are no applications to resubmit");
      return;
    }

    applicationsToResubmit.forEach(applicationStatus -> {
      String id = applicationStatus.getApplicationId();
      MDC.put("applicationId", id);
      Document document = applicationStatus.getDocumentType();
      String routingDestinationName = applicationStatus.getRoutingDestinationName();
      log.info("Resubmitting " + document.name() + "(s) to " + routingDestinationName
               + " for application id " + id);
      try {
        Application application = applicationRepository.find(id);
        RoutingDestination routingDestination = routingDecisionService.getRoutingDestinationByName(
            routingDestinationName);
        if (document == UPLOADED_DOC) {
          resubmitUploadedDocumentsForApplication(document, application,
              routingDestination.getEmail());
        } else {
          var applicationFile = pdfGenerator.generate(application, document, CASEWORKER);
          emailClient.resubmitFailedEmail(routingDestination.getEmail(), document, applicationFile,
              application);
        }
        documentStatusRepository.createOrUpdate(id, document, routingDestinationName, DELIVERED);
        log.info("Resubmitted %s(s) for application id %s".formatted(document.name(), id));
      } catch (Exception e) {
        log.error("Failed to resubmit application %s via email".formatted(id), e);
        documentStatusRepository.createOrUpdate(id, document, routingDestinationName,
            RESUBMISSION_FAILED);
      }
    });
    MDC.clear();
  }

  @Scheduled(
      fixedDelayString = "${in-progress-resubmission.interval.milliseconds}", // how often to run (every 12 hours)
      initialDelayString = "${in-progress-resubmission.initialDelay.milliseconds:0}"
  )
  @SchedulerLock(name = "resubmissionTask", lockAtMostFor = "30m")
  public void resubmitInProgressApplicationsViaEsb() {
    log.info("Checking for applications that are stuck in progress");

    // Publish ApplicationSubmittedEvents for CAF and CCAPs
    List<Application> cafAndCcapApplicationsToResubmit = applicationRepository.findApplicationsStuckInProgress();
    for (Application application : cafAndCcapApplicationsToResubmit) {
      log.info(
          "Retriggering CAF and/or CCAP submission for application with id " + application.getId());
      // TODO certain pops too
      MDC.put("applicationId", application.getId());

      pageEventPublisher.publish(
          new ApplicationSubmittedEvent("resubmission", application.getId(), application.getFlow(),
              application.getApplicationData().getLocale()));
    }

    // Publish UploadedDocumentSubmittedEvents for literally everything else
    List<Application> uploadedDocumentsToResubmit = applicationRepository.findUploadedDocumentsStuckInProgress();
    for (Application application : uploadedDocumentsToResubmit) {
      log.info("Retriggering UPLOADED_DOCs for application with id " + application.getId());
      MDC.put("applicationId", application.getId());

      pageEventPublisher.publish(
          new UploadedDocumentsSubmittedEvent("resubmission", application.getId(),
              application.getApplicationData().getLocale()));
    }

    MDC.clear();
  }

  private void resubmitUploadedDocumentsForApplication(Document document, Application application,
      String recipientEmail) {
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
      emailClient.resubmitFailedEmail(recipientEmail, document, fileToSend, application);
      log.info("Finished resubmitting document %s".formatted(esbFilename));
    }
  }
}
