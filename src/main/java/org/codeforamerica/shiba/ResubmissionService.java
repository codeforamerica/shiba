package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.County.Sherburne;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
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
  private FeatureFlagConfiguration featureFlagConfiguration;


  public ResubmissionService(ApplicationRepository applicationRepository,
      EmailClient emailClient,
      PdfGenerator pdfGenerator,
      RoutingDecisionService routingDecisionService,
      DocumentStatusRepository documentStatusRepository,
      PageEventPublisher pageEventPublisher,
      FeatureFlagConfiguration featureFlagConfiguration) {
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.pdfGenerator = pdfGenerator;
    this.routingDecisionService = routingDecisionService;
    this.documentStatusRepository = documentStatusRepository;
    this.pageEventPublisher = pageEventPublisher;
    this.featureFlagConfiguration = featureFlagConfiguration;
  }

  @Scheduled(
      fixedDelayString = "${failed-resubmission.interval.milliseconds}",
      initialDelayString = "${failed-resubmission.initialDelay.milliseconds:0}"
  )
  @SchedulerLock(name = "emailResubmissionTask", lockAtMostFor = "${failed-resubmission.lockAtMostFor}", lockAtLeastFor = "${failed-resubmission.lockAtLeastFor}")
  public void resubmitFailedApplications() {
    log.info("Checking for applications that failed to send");
    List<DocumentStatus> applicationsToResubmit = documentStatusRepository.getDocumentStatusToResubmit();

    if (applicationsToResubmit.isEmpty()) {
      log.info("There are no applications to resubmit from failure status");
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
      fixedDelayString = "${in-progress-resubmission.interval.milliseconds}",
      initialDelayString = "${in-progress-resubmission.initialDelay.milliseconds:0}"
  )
  @SchedulerLock(name = "esbResubmissionTask", lockAtMostFor = "${in-progress-resubmission.lockAtMostFor}", lockAtLeastFor = "${in-progress-resubmission.lockAtLeastFor}")
  public void resubmitInProgressAndSendingApplicationsViaEsb() {
    log.info("Checking for applications that are stuck in progress");

    List<Application> applicationsStuckInProgress = applicationRepository.findApplicationsStuckInProgressAndSending();
    log.info(
        "Resubmitting " + applicationsStuckInProgress.size() + " applications stuck in_progress");

    for (Application application : applicationsStuckInProgress) {
      String id = application.getId();
      // Add applicationId to the logs to make it easier to query for in datadog
      MDC.put("applicationId", id);
      log.info("Retriggering submission for application with id " + id);

      sendDocumentsViaESB(application, id);
    }

    // remove last applicationId from the mdc so it doesn't pollute future logs
    MDC.clear();
  }

  @Scheduled(
      fixedDelayString = "${no-status-applications-resubmission.interval.milliseconds}",
      initialDelayString = "${no-status-applications-resubmission.initialDelay.milliseconds:0}"
  )
  @SchedulerLock(name = "noStatusEsbResubmissionTask", lockAtMostFor = "${no-status-applications-resubmission.lockAtMostFor}", lockAtLeastFor = "${no-status-applications-resubmission.lockAtLeastFor}")
  public void resubmitBlankStatusApplicationsViaEsb() {
    log.info("Checking for applications that have no statuses");

    //get list back from db of blank status applications from Sherburne
    List<Application> applicationsWithBlankStatuses = new ArrayList<Application>();

    if (featureFlagConfiguration.get("only-submit-blank-status-apps-from-sherburne").isOn()) {
      applicationsWithBlankStatuses = applicationRepository.findApplicationsWithBlankStatuses(
          Sherburne);
    } else {
      applicationsWithBlankStatuses = applicationRepository.findApplicationsWithBlankStatuses();
    }
    log.info(
        "Resubmitting " + applicationsWithBlankStatuses.size() + " applications with no statuses");

    //from applicationData, decide on what docs need to be created
    for (Application application : applicationsWithBlankStatuses) {
      String id = application.getId();
      // Add applicationId to the logs to make it easier to query for in datadog
      MDC.put("applicationId", id);
      log.info("Retriggering submission for application with id " + id);

      documentStatusRepository.createOrUpdateAll(application, SENDING);
      ZonedDateTime sixtyDaysAgo = ZonedDateTime.now().minus(Duration.ofDays(60));

      if (!application.getApplicationData().getUploadedDocs().isEmpty() &&
       application.getCompletedAt().isAfter(sixtyDaysAgo)) {
        documentStatusRepository.createOrUpdateAllForDocumentType(application.getApplicationData(),
            SENDING, UPLOADED_DOC);
      }

      Application retrievedApp = applicationRepository.find(id);

      sendDocumentsViaESB(retrievedApp, id);
    }
    //resend application docs
    MDC.clear();

  }

  private void sendDocumentsViaESB(Application application, String id) {
    List<Document> inProgressDocs = application.getDocumentStatuses().stream()
        .filter(documentStatus -> documentStatus.getStatus().equals(IN_PROGRESS)
            || documentStatus.getStatus().equals(SENDING) &&
            List.of(CAF, CCAP, CERTAIN_POPS, UPLOADED_DOC)
                .contains(documentStatus.getDocumentType())).map(
            DocumentStatus::getDocumentType
        ).collect(Collectors.toList());

    documentStatusRepository.delete(id, inProgressDocs);

    boolean shouldRefireAppSubmittedEvent = inProgressDocs.stream()
        .anyMatch(document -> List.of(CAF, CCAP, CERTAIN_POPS).contains(document));
    if (shouldRefireAppSubmittedEvent) {
      log.info("Retriggering ApplicationSubmittedEvent for application with id " + id);
      pageEventPublisher.publish(new ApplicationSubmittedEvent("resubmission", id,
          application.getFlow(),
          application.getApplicationData().getLocale()));
    }

    boolean shouldRefireUploadedDocSubmittedEvent = inProgressDocs.stream()
        .anyMatch(document -> Objects.equals(
            UPLOADED_DOC, document));
    if (shouldRefireUploadedDocSubmittedEvent) {
      log.info("Retriggering UploadedDocumentsSubmittedEvent for application with id " + id);
      pageEventPublisher.publish(
          new UploadedDocumentsSubmittedEvent("resubmission", id,
              application.getApplicationData().getLocale()));
    }
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
