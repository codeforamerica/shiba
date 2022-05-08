package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.application.Status.UNDELIVERABLE;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Document.XML;
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
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
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
import org.codeforamerica.shiba.statemachine.StatesAndEvents;
import org.jetbrains.annotations.NotNull;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MnitDocumentConsumer {

  private final EmailClient emailClient;
  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final MonitoringService monitoringService;
  private final RoutingDecisionService routingDecisionService;
  private final ApplicationStatusRepository applicationStatusRepository;
  private final FeatureFlagConfiguration featureFlagConfiguration;
  private final FilenetWebServiceClient mnitFilenetClient;
  private final FilenameGenerator filenameGenerator;
  private final StateMachineService stateMachineService;

  public MnitDocumentConsumer(EmailClient emailClient,
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
      RoutingDecisionService routingDecisionService,
      ApplicationStatusRepository applicationStatusRepository,
      FeatureFlagConfiguration featureFlagConfiguration,
      FilenetWebServiceClient mnitFilenetClient,
      FilenameGenerator filenameGenerator,
      StateMachineService stateMachineService ) {
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.monitoringService = monitoringService;
    this.routingDecisionService = routingDecisionService;
    this.applicationStatusRepository = applicationStatusRepository;
    this.emailClient = emailClient;
    this.featureFlagConfiguration = featureFlagConfiguration;
    this.mnitFilenetClient = mnitFilenetClient;
    this.filenameGenerator = filenameGenerator;
    this.stateMachineService = stateMachineService;
  }

  public void processCafAndCcap(Application application) {
    String id = application.getId();
    monitoringService.setApplicationId(id);

    // Send the CAF, CCAP, and XML files in parallel
    List<Thread> threads = createThreadsForSendingThisApplication(application, id);
    StateMachine<StatesAndEvents.DeliveryStates, StatesAndEvents.DeliveryEvents> machine =
            this.stateMachineService.acquireStateMachine(application.getId());
    machine.sendEvent(StatesAndEvents.DeliveryEvents.SENDING_DOC);

    // Wait for everything to finish before returning
    threads.forEach(thread -> {
      try {
        thread.join();
        machine.sendEvent(StatesAndEvents.DeliveryEvents.DELIVERY_SUCCESS);
      } catch (InterruptedException e) {
        machine.sendEvent(StatesAndEvents.DeliveryEvents.SEND_ERROR);
        log.error("Thread interrupted for application with id " + application.getId(), e);
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
        log.info(
            "Started Thread for: " + doc.name() + " with filename: " + pdf.getFileName() + " to: "
                + rd.getName());
        threads.add(thread);
      }
    });

    // Create threads for sending the xml to each recipient who also received a PDF
    allRoutingDestinations.forEach(rd -> {
      ApplicationFile xml = xmlGenerator.generate(id, CAF, CASEWORKER);
      Thread thread = new Thread(() -> sendFile(application, XML, xml, rd));
      thread.start();
      threads.add(thread);
    });
    return threads;
  }

  public void processUploadedDocuments(Application application) {
    List<ApplicationFile> uploadedDocs = prepareUploadedDocsForSending(application);
    if (uploadedDocs.isEmpty()) {
      log.error(
          "There was an issue processing and delivering uploaded documents. Reach out to client to upload again.");
      applicationStatusRepository.createOrUpdateAllForDocumentType(application,
          UNDELIVERABLE, UPLOADED_DOC);
      return;
    }

    List<RoutingDestination> routingDestinations = routingDecisionService
        .getRoutingDestinations(application.getApplicationData(), UPLOADED_DOC);
    for (RoutingDestination routingDestination : routingDestinations) {
      boolean sendXMLToDakota = routingDestination.getName().equals(County.Dakota.name())
          && application.getFlow() == FlowType.LATER_DOCS;
      if (sendXMLToDakota) {
        ApplicationFile xml = xmlGenerator.generate(application.getId(), XML, CASEWORKER);
        sendFileAndUpdateStatus(application, XML, xml, routingDestination);
      }
      log.info("Uploaded docs to submit %s".formatted(uploadedDocs.size()));
      for (int i = 0; i < uploadedDocs.size(); i++) {
        ApplicationFile uploadedDoc = uploadedDocs.get(i);
        // rename file with filename that is specific to this destination
        String extension = Utils.getFileType(uploadedDoc.getFileName());
        String newFilename = filenameGenerator.generateUploadedDocumentName(application, i,
            extension, routingDestination, uploadedDocs.size());
        ApplicationFile renamedFile = new ApplicationFile(uploadedDoc.getFileBytes(),
            newFilename);
        sendFileAndUpdateStatus(application, UPLOADED_DOC, renamedFile, routingDestination);
      }
    }
  }

  /**
   * Returns a list of uploaded docs that have been renamed to meet the standards of MNIT, converted
   * to PDFs (when possible), and have had cover pages added (when possible) Note: the filenames for
   * these documents will include the county dhsProviderId. Those filenames are changed later to
   * include the dhsProviderId specific to whatever RoutingDestination the file is being sent to
   */
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
      applicationStatusRepository.createOrUpdate(id, documentType, routingDestination.getName(),
          SENDING,applicationFile.getFileName());
      sendFile(application, documentType, applicationFile, routingDestination);
    } catch (Exception e) {
      applicationStatusRepository.createOrUpdate(id, documentType, routingDestination.getName(),
          DELIVERY_FAILED,applicationFile.getFileName());
      log.error("Failed to send document %s to recipient %s for application %s with error, "
          .formatted(documentType, routingDestination.getName(), id), e);
    }
  }

  private void sendFile(Application application, Document document, ApplicationFile file,
      RoutingDestination routingDestination) {

    // This is where we want to generate the new filename
    log.info("Now sending %s to recipient %s for application %s".formatted(
        document.name(),
        routingDestination.getName(),
        application.getId()));

    mnitFilenetClient.send(file, routingDestination, application.getId(), document,
        application.getFlow());

  }
}