package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.County.MilleLacsBand;
import static org.codeforamerica.shiba.TribalNation.MILLE_LACS;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.RoutingDestinationService;
import org.codeforamerica.shiba.pages.RoutingDestinationService.RoutingDestination;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MnitDocumentConsumer {

  private final MnitEsbWebServiceClient mnitClient;
  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final MonitoringService monitoringService;
  private final RoutingDestinationService routingDestinationService;
  private final ApplicationRepository applicationRepository;
  private final FeatureFlagConfiguration featureFlagConfiguration;

  public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
      RoutingDestinationService routingDestinationService,
      ApplicationRepository applicationRepository,
      FeatureFlagConfiguration featureFlagConfiguration) {
    this.mnitClient = mnitClient;
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.monitoringService = monitoringService;
    this.routingDestinationService = routingDestinationService;
    this.applicationRepository = applicationRepository;
    this.featureFlagConfiguration = featureFlagConfiguration;
  }

  public void processCafAndCcap(Application application) {
    monitoringService.setApplicationId(application.getId());
    // Send the CAF and CCAP as PDFs
    DocumentListParser.parse(application.getApplicationData()).forEach(documentType -> {
      try {
        String id = application.getId();
        applicationRepository.updateStatus(id, documentType, SENDING);
        ApplicationFile applicationFile = pdfGenerator
            .generate(application.getId(), documentType, CASEWORKER);
        sendApplication(application, documentType, applicationFile);
      } catch (Exception e) {
        String id = application.getId();
        applicationRepository.updateStatus(id, documentType, DELIVERY_FAILED);
        log.error("Failed to send with error, ", e);
      }
    });

    // Send the CAF as XML
    sendApplication(application, CAF, xmlGenerator.generate(application.getId(), CAF, CASEWORKER));
  }

  public void processUploadedDocuments(Application application) {
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, SENDING);
    List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
    byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument uploadedDocument = uploadedDocs.get(i);
      ApplicationFile fileToSend = pdfGenerator.generateForUploadedDocument(uploadedDocument, i,
          application, coverPage);
      if (fileToSend != null && fileToSend.getFileBytes().length > 0) {
        log.info("Now sending: " + fileToSend.getFileName() + " original filename: "
            + uploadedDocument.getFilename());
        sendApplication(application, UPLOADED_DOC, fileToSend);
        log.info("Finished sending document " + fileToSend.getFileName());
      } else {
        log.error("Skipped uploading file " + uploadedDocument.getFilename()
            + " because it was empty. This should only happen in a dev environment.");
      }
    }
  }

  private void sendApplication(Application application, Document document, ApplicationFile file) {
    RoutingDestination routingDestination = routingDestinationService
        .getRoutingDestination(application.getApplicationData(), document);
    if (shouldSendToMilleLacs(routingDestination, document)) {
      mnitClient.send(file, MilleLacsBand, application.getId(), document, application.getFlow());
    }

    if (!shouldSendToMilleLacs(routingDestination, document)
        || routingDestination.getCounty() != null) {
      mnitClient.send(file, application.getCounty(), application.getId(), document,
          application.getFlow());
    }
  }

  private boolean shouldSendToMilleLacs(RoutingDestination routingDestination, Document document) {
    return featureFlagConfiguration.get("apply-for-mille-lacs").isOn()
        && routingDestination.getTribalNation() != null
        && routingDestination.getTribalNation().equals(MILLE_LACS)
        && !CCAP.equals(document);
  }
}
