package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
<<<<<<< Upstream, based on branch 'mnit-filenet-feature' of git@github.com:codeforamerica/shiba
import org.codeforamerica.shiba.mnit.MnitFilenetWebServiceClient;
=======
import org.codeforamerica.shiba.mnit.RoutingDestination;
>>>>>>> e67f964 Counties and tribal nations have common ancestor (#442)
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
<<<<<<< Upstream, based on branch 'mnit-filenet-feature' of git@github.com:codeforamerica/shiba
import org.codeforamerica.shiba.pages.RoutingDestinationService;
import org.codeforamerica.shiba.pages.RoutingDestinationService.RoutingDestination;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
=======
import org.codeforamerica.shiba.pages.RoutingDecisionService;
>>>>>>> e67f964 Counties and tribal nations have common ancestor (#442)
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MnitDocumentConsumer {

  private final MnitEsbWebServiceClient mnitClient;
  private final MnitFilenetWebServiceClient mnitFilenetClient;
  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final MonitoringService monitoringService;
  private final RoutingDecisionService routingDecisionService;
  private final ApplicationRepository applicationRepository;
  private final FeatureFlagConfiguration featureFlags;
  private final HashMap<String, TribalNationRoutingDestination> tribalNations;
  private final CountyMap<CountyRoutingDestination> countyMap;

  public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
	  MnitFilenetWebServiceClient mnitFilenetClient,
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
      RoutingDestinationService routingDestinationService,
      ApplicationRepository applicationRepository,
      FeatureFlagConfiguration featureFlags) {
	this.mnitClient = mnitClient;
    this.mnitFilenetClient = mnitFilenetClient;
      RoutingDecisionService routingDecisionService,
      ApplicationRepository applicationRepository,
      HashMap<String, TribalNationRoutingDestination> tribalNations,
      CountyMap<CountyRoutingDestination> countyMap) {
    this.mnitClient = mnitClient;
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.monitoringService = monitoringService;
    this.routingDecisionService = routingDecisionService;
    this.applicationRepository = applicationRepository;
    this.featureFlags = featureFlags;

    this.tribalNations = tribalNations;
    this.countyMap = countyMap;
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
    List<RoutingDestination> routingDestinations = routingDecisionService
        .getRoutingDestinations(application.getApplicationData(), document);

    if (featureFlags.get("filenet").isOn()) {
        if (MilleLacsBand.displayName().equals(routingDestination.getTribalNation())) {
            mnitFilenetClient.send(file, MilleLacsBand, application.getId(), document, application.getFlow());
          }
          if (routingDestination.getCounty() != null) {
            mnitFilenetClient.send(file, application.getCounty(), application.getId(), document,
                application.getFlow());
          }
    } else {
        if (MilleLacsBand.displayName().equals(routingDestination.getTribalNation())) {
            mnitClient.send(file, MilleLacsBand, application.getId(), document, application.getFlow());
          }
          if (routingDestination.getCounty() != null) {
            mnitClient.send(file, application.getCounty(), application.getId(), document,
                application.getFlow());
          }
    }
    routingDestinations.forEach(rd -> {
      mnitClient.send(file, rd, application.getId(), document, application.getFlow());
    });
  }
}
