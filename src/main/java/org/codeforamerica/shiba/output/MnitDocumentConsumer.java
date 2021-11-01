package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.AlfrescoWebServiceClient;
import org.codeforamerica.shiba.mnit.FilenetWebServiceClient;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.EmailClient;
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

  public MnitDocumentConsumer(AlfrescoWebServiceClient mnitClient,
      EmailClient emailClient,
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
      RoutingDecisionService routingDecisionService,
      ApplicationRepository applicationRepository,
      FeatureFlagConfiguration featureFlagConfiguration,
      FilenetWebServiceClient mnitFilenetClient) {
    this.mnitClient = mnitClient;
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.monitoringService = monitoringService;
    this.routingDecisionService = routingDecisionService;
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.featureFlagConfiguration = featureFlagConfiguration;
    this.mnitFilenetClient = mnitFilenetClient;
  }

  public void processCafAndCcap(Application application) {
    String id = application.getId();
    monitoringService.setApplicationId(id);

    List<Thread> threads = new ArrayList<>();

    // Generate the CAF and CCAP PDFs to send in parallel so that one document isn't waiting on the
    // other to finish sending.
    for (Document doc : DocumentListParser.parse(application.getApplicationData())) {
      List<RoutingDestination> routingDestinations =
          routingDecisionService.getRoutingDestinations(application.getApplicationData(), doc);

      // add a thread to send the pdf for each routing destination
      for (RoutingDestination routingDestination : routingDestinations) {
        ApplicationFile pdf = pdfGenerator.generate(id, doc, CASEWORKER, routingDestination);
        threads.add(new Thread(new SendFileRunnable(doc, pdf, application, routingDestination)));

        // also add xml if this document is the CAF
        if (doc.equals(CAF)) {
          ApplicationFile xml = xmlGenerator.generate(id, CAF, CASEWORKER);
          threads.add(new Thread(new SendFileRunnable(doc, xml, application, routingDestination)));
        }
      }
    }

    // Send the CAF, CCAP, and XML files in parallel
    threads.forEach(Thread::start);

    // Wait for everything to finish before returning
    threads.forEach(t -> {
      try {
        t.join();
      } catch (InterruptedException e) {
        log.error("Thread interrupted", e);
      }
    });
  }

  public void processUploadedDocuments(Application application) {
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, SENDING);
    List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
    List<ApplicationFile> applicationFiles = new ArrayList<>();

    // General files to send
    byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument uploadedDocument = uploadedDocs.get(i);
      ApplicationFile fileToSend = pdfGenerator.generateForUploadedDocument(uploadedDocument, i,
          application, coverPage);
      if (fileToSend != null && fileToSend.getFileBytes().length > 0) {
        log.info("Now queueing file to send: %s".formatted(fileToSend.getFileName()));
        applicationFiles.add(fileToSend);
      } else {
        // This should only happen in a dev environment
        log.error(
            "Skipped uploading file " + uploadedDocument.getFilename() + " because it was empty.");
      }
    }

    // Send files
    List<RoutingDestination> routingDestinations = routingDecisionService
        .getRoutingDestinations(application.getApplicationData(), UPLOADED_DOC);
    for (RoutingDestination rd : routingDestinations) {
      boolean sendToHennepinViaEmail = featureFlagConfiguration.get(
          "submit-docs-via-email-for-hennepin").isOn();
      boolean isHennepin = rd.getName().equals(County.Hennepin.name());

      if (sendToHennepinViaEmail && isHennepin) {
        emailClient.sendHennepinDocUploadsEmails(application, applicationFiles);
      } else {
        for (ApplicationFile fileToSend : applicationFiles) {
          sendFileToRoutingDestination(application, UPLOADED_DOC, fileToSend, rd);
        }
      }
    }

    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, DELIVERED);
  }


  private void sendFileToRoutingDestination(Application application, Document document,
      ApplicationFile file, RoutingDestination routingDestination) {

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

  class SendFileRunnable implements Runnable {

    private final Document documentType;
    private final ApplicationFile applicationFile;
    private final Application application;
    private final RoutingDestination routingDestination;

    public SendFileRunnable(Document documentType, ApplicationFile applicationFile,
        Application application,
        RoutingDestination routingDestination) {
      this.documentType = documentType;
      this.applicationFile = applicationFile;
      this.application = application;
      this.routingDestination = routingDestination;
    }

    @Override
    public void run() {
      String id = application.getId();
      try {
        applicationRepository.updateStatus(id, documentType, SENDING);
        sendFileToRoutingDestination(application, documentType, applicationFile,
            routingDestination);
      } catch (Exception e) {
        applicationRepository.updateStatus(id, documentType, DELIVERY_FAILED);
        log.error(
            "Failed to send document %s to recipient %s for application %s with error, ".formatted(
                documentType, routingDestination.getName(), id), e);
      }
    }
  }
}