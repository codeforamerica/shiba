package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.Utils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.AlfrescoWebServiceClient;
import org.codeforamerica.shiba.mnit.FilenetWebServiceClient;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MnitDocumentConsumer {

  private final AlfrescoWebServiceClient mnitClient;
  private final EmailClient emailClient;
  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final MonitoringService monitoringService;
  private final RoutingDecisionService routingDecisionService;
  private final ApplicationRepository applicationRepository;
  private final FeatureFlagConfiguration featureFlagConfiguration;
  private final FilenetWebServiceClient mnitFilenetClient;
  private final FilenameGenerator filenameGenerator;

  public MnitDocumentConsumer(AlfrescoWebServiceClient mnitClient,
      EmailClient emailClient,
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
      RoutingDecisionService routingDecisionService,
      ApplicationRepository applicationRepository,
      FeatureFlagConfiguration featureFlagConfiguration,
      FilenetWebServiceClient mnitFilenetClient,
      FilenameGenerator filenameGenerator) {
    this.mnitClient = mnitClient;
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.monitoringService = monitoringService;
    this.routingDecisionService = routingDecisionService;
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.featureFlagConfiguration = featureFlagConfiguration;
    this.mnitFilenetClient = mnitFilenetClient;
    this.filenameGenerator = filenameGenerator;
  }

  public void processCafAndCcap(Application application) {
    String id = application.getId();
    monitoringService.setApplicationId(id);

    // Send the CAF, CCAP, and XML files in parallel
    List<Thread> threads = createThreadsForSendingThisApplication(application, id);

    // Wait for everything to finish before returning
    threads.forEach(thread -> {
      try {
        thread.join();
      } catch (InterruptedException e) {
        log.error("Thread interrupted", e);
      }
    });
  }

  @NotNull
  private List<Thread> createThreadsForSendingThisApplication(Application application, String id) {
    List<Thread> threads = new ArrayList<>();
    Set<RoutingDestination> allRoutingDestinations = new HashSet<>();

    ApplicationData applicationData = application.getApplicationData();

    // Create threads for sending CAF & CCAP pdfs to each recipient
    DocumentListParser.parse(applicationData).forEach(doc -> {
      List<RoutingDestination> routingDestinations =
          routingDecisionService.getRoutingDestinations(applicationData, doc);

      // Keep track of routing destinations so we know who to send the XML to later
      allRoutingDestinations.addAll(routingDestinations);

      for (RoutingDestination rd : routingDestinations) {
        ApplicationFile pdf = pdfGenerator.generate(id, doc, CASEWORKER, rd);
        Thread thread = new Thread(() -> sendFileAndUpdateStatus(application, doc, pdf, rd));
        thread.start();
        threads.add(thread);
      }
    });

    // Create threads for sending the xml to each recipient who also received a PDF
    allRoutingDestinations.forEach(rd -> {
      ApplicationFile xml = xmlGenerator.generate(id, CAF, CASEWORKER);
      Thread thread = new Thread(() -> sendFile(application, CAF, xml, rd));
      thread.start();
      threads.add(thread);
    });
    return threads;
  }

  public void processUploadedDocuments(Application application) {
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, SENDING);
    List<ApplicationFile> uploadedDocs = prepareUploadedDocsForSending(application);
    sendUploadedDocs(application, uploadedDocs);
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, DELIVERED);
  }

  private void sendUploadedDocs(Application application, List<ApplicationFile> uploadedDocs) {
    List<RoutingDestination> routingDestinations = routingDecisionService
        .getRoutingDestinations(application.getApplicationData(), UPLOADED_DOC);

    for (RoutingDestination routingDestination : routingDestinations) {
      boolean sendToHennepinViaEmail = featureFlagConfiguration.get(
          "submit-docs-via-email-for-hennepin").isOn();
      boolean isHennepin = routingDestination.getName().equals(County.Hennepin.name());

      if (sendToHennepinViaEmail && isHennepin) {
        emailClient.sendHennepinDocUploadsEmails(application, uploadedDocs);
      } else {
        for (int i = 0; i < uploadedDocs.size(); i++) {
          ApplicationFile uploadedDoc = uploadedDocs.get(i);
          // rename file with filename that is specific to this destination
          String extension = Utils.getFileType(uploadedDoc.getFileName());
          String newFilename = filenameGenerator.generateUploadedDocumentName(application, i,
              extension, routingDestination);
          ApplicationFile renamedFile = new ApplicationFile(uploadedDoc.getFileBytes(),
              newFilename);
          sendFile(application, UPLOADED_DOC, renamedFile, routingDestination);
        }
      }
    }
  }

  // Returns a list of uploaded docs that have been renamed to meet the standards of MNIT, converted
  // to PDFs (when possible), and have had cover pages added (when possible)
  //
  // Note: the filenames for these documents will include the county dhsProviderId. Those filenames
  // are changed later to include the dhsProviderId specific to whatever RoutingDestination the file
  // is being sent to
  private List<ApplicationFile> prepareUploadedDocsForSending(Application application) {
    var uploadedDocs = application.getApplicationData().getUploadedDocs();
    List<ApplicationFile> applicationFiles = new ArrayList<>();
    byte[] coverPage = pdfGenerator.generateCoverPageForUploadedDocs(application);
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument originalDocument = uploadedDocs.get(i);
      ApplicationFile preparedDocument =
          pdfGenerator.generateForUploadedDocument(originalDocument, i, application, coverPage);
      if (preparedDocument != null && preparedDocument.getFileBytes().length > 0) {
        log.info("Now queueing file to send: %s".formatted(preparedDocument.getFileName()));
        applicationFiles.add(preparedDocument);
      } else {
        // This should only happen in a dev environment
        log.error("Skipped uploading file %s because it was empty.".formatted(
            originalDocument.getFilename()));
      }
    }
    return applicationFiles;
  }

  private void sendFileAndUpdateStatus(Application application, Document documentType,
      ApplicationFile applicationFile, RoutingDestination routingDestination) {

    String id = application.getId();
    try {
      applicationRepository.updateStatus(id, documentType, SENDING);
      sendFile(application, documentType, applicationFile,
          routingDestination);
    } catch (Exception e) {
      applicationRepository.updateStatus(id, documentType, DELIVERY_FAILED);
      log.error("Failed to send document %s to recipient %s for application %s with error, "
          .formatted(documentType, routingDestination.getName(), id), e);
    }
  }

  private void sendFile(Application application, Document document, ApplicationFile file,
      RoutingDestination routingDestination) {

    String documentName = document.name();
    if (file != null && file.getFileName() != null && file.getFileName().contains("xml")) {
      documentName = "XML";
    }
    // This is where we want to generate the new filename
    log.info("Now sending %s to recipient %s for application %s".formatted(
        documentName,
        routingDestination.getName(),
        application.getId()));
    if (featureFlagConfiguration.get("filenet").isOn()) {
      mnitFilenetClient.send(file, routingDestination, application.getId(), document,
          application.getFlow());
    } else {
      mnitClient.send(file, routingDestination, application.getId(), document,
          application.getFlow());
    }
  }
}