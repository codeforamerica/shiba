package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.County.MilleLacsBand;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MnitDocumentConsumer {

  private final MnitEsbWebServiceClient mnitClient;
  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final MonitoringService monitoringService;
  private final String activeProfile;
  private final RoutingDestinationService routingDestinationService;
  private final ApplicationRepository applicationRepository;
  private final FeatureFlagConfiguration featureFlagConfiguration;

  public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
      @Value("${spring.profiles.active:dev}") String activeProfile,
      RoutingDestinationService routingDestinationService,
      ApplicationRepository applicationRepository,
      FeatureFlagConfiguration featureFlagConfiguration) {
    this.mnitClient = mnitClient;
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.monitoringService = monitoringService;
    this.activeProfile = activeProfile;
    this.routingDestinationService = routingDestinationService;
    this.applicationRepository = applicationRepository;
    this.featureFlagConfiguration = featureFlagConfiguration;
  }

  public void process(Application application) {
    monitoringService.setApplicationId(application.getId());
    // Send the CAF and CCAP as PDFs
    RoutingDestination routingDestination = routingDestinationService
        .getRoutingDestination(application.getApplicationData());
    DocumentListParser.parse(application.getApplicationData()).forEach(documentType -> {
      try {
        String id = application.getId();
        applicationRepository.updateStatus(id, documentType, SENDING);
        ApplicationFile applicationFile = pdfGenerator
            .generate(application.getId(), documentType, CASEWORKER);
        sendApplication(routingDestination, application, documentType, applicationFile);
      } catch (Exception e) {
        String id = application.getId();
        applicationRepository.updateStatus(id, documentType, DELIVERY_FAILED);
        log.error("Failed to send with error, ", e);
      }
    });
    sendApplication(routingDestination, application, CAF,
        xmlGenerator.generate(application.getId(), CAF, CASEWORKER));
  }

  public void processUploadedDocuments(Application application) {
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, SENDING);
    List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
    byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
    RoutingDestination routingDestination = routingDestinationService
        .getRoutingDestination(application.getApplicationData());
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument uploadedDocument = uploadedDocs.get(i);
      ApplicationFile fileToSend = pdfGenerator.generateForUploadedDocument(uploadedDocument, i,
          application, coverPage);
      if (fileToSend != null && fileToSend.getFileBytes().length > 0) {
        log.info("Now sending: " + fileToSend.getFileName() + " original filename: "
            + uploadedDocument.getFilename());
        sendApplication(routingDestination, application, UPLOADED_DOC, fileToSend);
        log.info("Finished sending document " + fileToSend.getFileName());
      } else {
        log.error("Skipped uploading file " + uploadedDocument.getFilename()
            + " because it was empty. This should only happen in a dev environment.");
      }
    }
  }

  private void sendApplication(RoutingDestination routingDestination, Application application,
      Document documentType, ApplicationFile applicationFile) {

    // Only sending to Mille Lacs Band right now
    if (featureFlagConfiguration.get("apply-for-mille-lacs").isOn()
        && routingDestination.getTribalNation() != null) {
      mnitClient.send(applicationFile, MilleLacsBand, application.getId(), documentType,
          application.getFlow());
    }

    // County is parsed from home-address, but we dont want to send if it's not applicable for
    // tribal nations
    if (featureFlagConfiguration.get("apply-for-mille-lacs").isOff()
        || routingDestination.getCounty() != null) {
      mnitClient.send(applicationFile, application.getCounty(), application.getId(), documentType,
          application.getFlow());
    }
  }
}
