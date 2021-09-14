package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.TribalNation.MILLE_LACS;
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
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDestinationService;
import org.codeforamerica.shiba.pages.RoutingDestinationService.RoutingDestination;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResubmissionService {

  private final ApplicationRepository applicationRepository;
  private final MailGunEmailClient emailClient;
  private final CountyMap<MnitCountyInformation> countyMap;
  private final PdfGenerator pdfGenerator;
  private final RoutingDestinationService routingDestinationService;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public ResubmissionService(ApplicationRepository applicationRepository,
      MailGunEmailClient emailClient, CountyMap<MnitCountyInformation> countyMap,
      PdfGenerator pdfGenerator,
      RoutingDestinationService routingDestinationService) {
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.countyMap = countyMap;
    this.pdfGenerator = pdfGenerator;
    this.routingDestinationService = routingDestinationService;
  }

  @Scheduled(fixedDelayString = "${resubmission.interval.milliseconds}")
  @SchedulerLock(name = "resubmissionTask", lockAtMostFor = "30m")
  public void resubmitFailedApplications() {
    log.info("Checking for applications that failed to send");
    Map<Document, List<String>> documentsToIds = applicationRepository
        .getApplicationIdsToResubmit();

    if (documentsToIds.values().stream().allMatch(List::isEmpty)) {
      log.info("There are no applications to resubmit");
      return;
    }

    documentsToIds.forEach((document, applicationIds) -> applicationIds.forEach(id -> {
      MDC.put("applicationId", id);
      log.info("Resubmitting " + document.name() + "(s) for application id " + id);
      Application application = applicationRepository.find(id);
      RoutingDestination routingDestination = routingDestinationService.getRoutingDestination(
          application.getApplicationData());
      List<String> recipientEmails = new ArrayList<>();

      if (routingDestination.getCounty() != null) {
        recipientEmails.add(countyMap.get(application.getCounty()).getEmail());
      }

      if (routingDestination.getTribalNation() != null && routingDestination.getTribalNation()
          .equals(MILLE_LACS)) {
        recipientEmails.add(countyMap.get(County.MilleLacsBand).getEmail());
      }

      try {
        if (document.equals(UPLOADED_DOC)) {
          resubmitUploadedDocumentsForApplication(document, application, recipientEmails);
        } else {
          var applicationFile = pdfGenerator.generate(application, document, CASEWORKER);
          recipientEmails.forEach(
              eml -> emailClient.resubmitFailedEmail(eml, document, applicationFile, application));
        }
        applicationRepository.updateStatus(id, document, DELIVERED);
        log.info("Resubmitted " + document.name() + "(s) for application id " + id);
      } catch (Exception e) {
        log.error("Failed to resubmit application " + id + " via email");
        applicationRepository.updateStatus(id, document, RESUBMISSION_FAILED);
      }
    }));
    MDC.remove("applicationId");
  }

  private void resubmitUploadedDocumentsForApplication(Document document, Application application,
      List<String> recipientEmails) {
    var coverPage = pdfGenerator.generate(application, document, CASEWORKER).getFileBytes();
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
