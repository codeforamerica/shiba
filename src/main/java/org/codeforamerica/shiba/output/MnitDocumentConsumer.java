package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.ArrayList;
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
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MnitDocumentConsumer {

  private final MnitEsbWebServiceClient mnitClient;
  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final MonitoringService monitoringService;
  private final RoutingDecisionService routingDecisionService;
  private final ApplicationRepository applicationRepository;
  private final HashMap<String, TribalNationRoutingDestination> tribalNations;
  private final CountyMap<CountyRoutingDestination> countyMap;

  public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
      XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
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

    this.tribalNations = tribalNations;
    this.countyMap = countyMap;
  }

  public void processCafAndCcap(Application application) {
    String id = application.getId();
    monitoringService.setApplicationId(id);

    List<Thread> threads = new ArrayList<>();

    // Generate the CAF and CCAP PDFs to send in parallel so that one document isn't waiting on the
    // other to finish sending. pdfGenerator may not be thread-safe
    DocumentListParser.parse(application.getApplicationData()).forEach(documentType -> {
          threads.add(new Thread(new SendPDFRunnable(
              documentType,
              pdfGenerator.generate(id, documentType, CASEWORKER),
              application)));
        }
    );

    // Send the CAF and CCAP as PDFs in parallel
    threads.forEach(Thread::start);

    // Send the CAF as XML
    sendApplication(application, CAF, xmlGenerator.generate(id, CAF, CASEWORKER));

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
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, DELIVERED);
  }

  private void sendApplication(Application application, Document document, ApplicationFile file) {
    List<RoutingDestination> routingDestinations = routingDecisionService
        .getRoutingDestinations(application.getApplicationData(), document);

    routingDestinations.forEach(rd -> {
      log.info("Now sending %s to recipient %s for application %s".formatted(document.name(),
          rd.getName(), application.getId()));
      mnitClient.send(file, rd, application.getId(), document, application.getFlow());
    });
  }

  class SendPDFRunnable implements Runnable {

    private final Document documentType;
    private final ApplicationFile applicationFile;
    private final Application application;

    public SendPDFRunnable(Document documentType, ApplicationFile applicationFile,
        Application application) {
      this.documentType = documentType;
      this.applicationFile = applicationFile;
      this.application = application;
    }

    @Override
    public void run() {
      try {
        applicationRepository.updateStatus(application.getId(), documentType, SENDING);
        sendApplication(application, documentType, applicationFile);
      } catch (Exception e) {
        applicationRepository.updateStatus(application.getId(), documentType, DELIVERY_FAILED);
        log.error("Failed to send with error, ", e);
      }
    }
  }
}
