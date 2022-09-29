package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.DELIVERED_BY_EMAIL;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.application.Status.UNDELIVERABLE;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatus;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
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
  private final ApplicationStatusRepository applicationStatusRepository;
  private final PageEventPublisher pageEventPublisher;


  public ResubmissionService(ApplicationRepository applicationRepository,
      EmailClient emailClient,
      PdfGenerator pdfGenerator,
      RoutingDecisionService routingDecisionService,
      ApplicationStatusRepository applicationStatusRepository,
      PageEventPublisher pageEventPublisher) {
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.pdfGenerator = pdfGenerator;
    this.routingDecisionService = routingDecisionService;
    this.applicationStatusRepository = applicationStatusRepository;
    this.pageEventPublisher = pageEventPublisher;
  }

  @Scheduled(
      fixedDelayString = "${failed-resubmission.interval.milliseconds}",
      initialDelayString = "${failed-resubmission.initialDelay.milliseconds:0}"
  )
  @SchedulerLock(name = "emailResubmissionTask", lockAtMostFor = "${failed-resubmission.lockAtMostFor}", lockAtLeastFor = "${failed-resubmission.lockAtLeastFor}")
  public void resubmitFailedApplications() {
    log.info("Checking for applications that failed to send");
    List<ApplicationStatus> applicationsToResubmit = applicationStatusRepository.getDocumentStatusToResubmit();

    MDC.put("failedApps", String.valueOf(applicationsToResubmit.size()));
    log.info("Resubmitting " + applicationsToResubmit.size() + " apps over email");
    if (applicationsToResubmit.isEmpty()) {
      log.info("There are no applications to resubmit from failure status");
      return;
    }

    applicationsToResubmit.forEach(applicationStatus -> {
      String id = applicationStatus.getApplicationId();
      MDC.put("applicationId", id);
      Document document = applicationStatus.getDocumentType();
      String routingDestinationName = applicationStatus.getRoutingDestinationName();
      String documentName = applicationStatus.getDocumentName();
      log.info("Resubmitting " + document.name() + "(s) to " + routingDestinationName
          + " for application id " + id);
      try {
        Application application = applicationRepository.find(id);
        RoutingDestination routingDestination = routingDecisionService.getRoutingDestinationByName(
            routingDestinationName);
        if (document == UPLOADED_DOC) {
          resubmitUploadedDocumentsForApplication(document, application,
              routingDestination.getEmail(), documentName, routingDestination);
        } else {
          var applicationFile = pdfGenerator.generate(application, document, CASEWORKER, routingDestination);
          emailClient.resubmitFailedEmail(routingDestination.getEmail(), document, applicationFile, application);
          log.info("Sending resubmit failed email to " + routingDestination.getEmail() + " for application " + id);
        }
        applicationStatusRepository.createOrUpdate(id, document, routingDestinationName, DELIVERED_BY_EMAIL, documentName);
        log.info("Resubmitted %s(s) for application id %s".formatted(document.name(), id));
      } catch (Exception e) {
        log.error("Failed to resubmit application %s via email".formatted(id), e);
        applicationStatusRepository.createOrUpdate(id, document, routingDestinationName,
            RESUBMISSION_FAILED, documentName);
      }
    });
    MDC.clear();
  }

  @Scheduled(
      fixedDelayString = "${in-progress-resubmission.interval.milliseconds}",
      initialDelayString = "${in-progress-resubmission.initialDelay.milliseconds:0}"
  )
  @SchedulerLock(name = "esbResubmissionTask", lockAtMostFor = "${in-progress-resubmission.lockAtMostFor}", lockAtLeastFor = "${in-progress-resubmission.lockAtLeastFor}")
  public void republishApplicationsInSendingStatus() {
    log.info("Checking for applications that are stuck in progress/sending");

    List<Application> applicationsStuckSending = applicationRepository.findApplicationsStuckSending();
    MDC.put("appsStuckSending", String.valueOf(applicationsStuckSending.size()));
    log.info(
        "Resubmitting " + applicationsStuckSending.size() + " applications stuck sending");

    for (Application application : applicationsStuckSending) {
      String id = application.getId();
      // Add applicationId to the logs to make it easier to query for in datadog
      MDC.put("applicationId", id);
      log.info("Retriggering submission for application with id " + id);

      sendDocumentsViaESB(application, id, true);
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
    List<Application> applicationsWithBlankStatusRows = applicationRepository.findApplicationsWithBlankStatuses();
    
    // Filter out applications where no programs were selected. This shouldn't be possible but it has occurred.
    ArrayList<Application> applicationsWithBlankStatuses = new ArrayList<Application>();
    for (Application application : applicationsWithBlankStatusRows) {
    	Set<String> programs = application.getApplicationData().getApplicantAndHouseholdMemberPrograms();
    	if (!(programs.size() == 1 && programs.contains("NONE"))) {
    		applicationsWithBlankStatuses.add(application);
    	}
    }

    MDC.put("blankStatusApps", String.valueOf(applicationsWithBlankStatuses.size()));
    log.info(
        "Resubmitting " + applicationsWithBlankStatuses.size() + " applications with no statuses");

    //from applicationData, decide on what docs need to be created
    for (Application application : applicationsWithBlankStatuses) {
      String id = application.getId();
      // Add applicationId to the logs to make it easier to query for in datadog
      MDC.put("applicationId", id);
      log.info("Retriggering submission for application with id " + id);

      applicationStatusRepository.createOrUpdateApplicationType(application, SENDING);

      if (application.getFlow().equals(LATER_DOCS) || !application.getApplicationData()
          .getUploadedDocs().isEmpty()) {
        applicationStatusRepository.createOrUpdateAllForDocumentType(application,
            SENDING, UPLOADED_DOC);
      }

      Application retrievedApp = applicationRepository.find(id);
      sendDocumentsViaESB(retrievedApp, id, false);
    }
    //resend application docs
    MDC.clear();
  }

  private void sendDocumentsViaESB(Application application, String id,
      boolean shouldDeleteDocumentStatuses) {
    // Resend sending applications (without verification docs)
    List<Document> documentTypesInSending = application.getApplicationStatuses().stream()
        .filter(documentStatus -> documentStatus.getStatus() == SENDING &&
            List.of(CAF, CCAP, CERTAIN_POPS)
                .contains(documentStatus.getDocumentType())).map(
            ApplicationStatus::getDocumentType
        ).toList();

    boolean shouldRefireAppSubmittedEvent = documentTypesInSending.stream()
        .anyMatch(document -> List.of(CAF, CCAP, CERTAIN_POPS).contains(document));
    if (shouldRefireAppSubmittedEvent) {
      log.info("Retriggering ApplicationSubmittedEvent for application with id " + id);
      pageEventPublisher.publish(new ApplicationSubmittedEvent("resubmission", id,
          application.getFlow(),
          application.getApplicationData().getLocale()));
    }

    // Resend sending verification docs on an application
    Optional<ApplicationStatus> uploadedDocStatus = application.getApplicationStatuses().stream()
        .filter(documentStatus -> documentStatus.getDocumentType() == UPLOADED_DOC
            && documentStatus.getStatus() == SENDING)
        .findFirst();
    if (uploadedDocStatus.isPresent()) {
      resendUploadDocAndUpdateStatus(application, id, shouldDeleteDocumentStatuses);
    }
  }

  private void resendUploadDocAndUpdateStatus(Application application, String id,
      boolean shouldDeleteDocumentStatuses) {
    if (shouldDeleteDocumentStatuses) {
      // Will be recreated on submit event
      applicationStatusRepository.delete(id, List.of(UPLOADED_DOC));
    }
    // No docs to deliver or all files the client uploaded contained 0 bytes of data
    if (application.getApplicationData().getUploadedDocs().isEmpty() ||
        application.getApplicationData().getUploadedDocs().stream()
            .allMatch(uploadedDocument -> uploadedDocument.getSize() == 0)) {
      applicationStatusRepository.createOrUpdateAllForDocumentType(application,
          UNDELIVERABLE, UPLOADED_DOC);
    } else {
      // Docs older than 60 days cannot be delivered due to retention policy
      ZonedDateTime sixtyDaysAgo = ZonedDateTime.now().minus(Duration.ofDays(60));
      if (application.getCompletedAt().isBefore(sixtyDaysAgo)) {
        applicationStatusRepository.createOrUpdateAllForDocumentType(application,
            UNDELIVERABLE, UPLOADED_DOC);
      } else {
        log.info("Retriggering UploadedDocumentsSubmittedEvent for application with id " + id);
        pageEventPublisher.publish(
            new UploadedDocumentsSubmittedEvent("resubmission", id,
                application.getApplicationData().getLocale()));
      }
    }
  }

  private void resubmitUploadedDocumentsForApplication(Document document, Application application,
      String recipientEmail, String documentName, RoutingDestination routingDestination) {
    var coverPage = pdfGenerator.generateCoverPageForUploadedDocs(application);
    var uploadedDocs = application.getApplicationData().getUploadedDocs();
    var failedDoc = uploadedDocs.stream()
        .filter(uploadedDoc -> uploadedDoc.getSysFileName().equals(documentName)).toList();
    List<ApplicationFile> fileToSend =
        pdfGenerator.generateCombinedUploadedDocument(List.of(failedDoc.get(0)), application, coverPage, routingDestination);
    var esbFilename = fileToSend.get(0).getFileName();
    var originalFilename = failedDoc.get(0).getFilename();
    log.info("Resubmitting uploaded doc: %s original filename: %s".formatted(esbFilename,
        originalFilename));
    emailClient.resubmitFailedEmail(recipientEmail, document, fileToSend.get(0), application);
    log.info("Finished resubmitting document %s".formatted(esbFilename));
  }
}
