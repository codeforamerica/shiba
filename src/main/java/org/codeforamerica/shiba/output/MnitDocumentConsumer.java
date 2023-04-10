package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.application.Status.UNDELIVERABLE;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Document.XML;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import java.util.ArrayList;
import java.util.Collections;
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
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MnitDocumentConsumer {

  private final XmlGenerator xmlGenerator;
  private final PdfGenerator pdfGenerator;
  private final MonitoringService monitoringService;
  private final RoutingDecisionService routingDecisionService;
  private final ApplicationStatusRepository applicationStatusRepository;
  private final FilenetWebServiceClient mnitClient;
  private final FilenameGenerator filenameGenerator;
  private final UploadedDocsPreparer uploadedDocsPreparer;
  private final ThreadPoolTaskExecutor executor;

  public MnitDocumentConsumer(XmlGenerator xmlGenerator,
      PdfGenerator pdfGenerator,
      MonitoringService monitoringService,
      RoutingDecisionService routingDecisionService,
      ApplicationStatusRepository applicationStatusRepository,
      FilenetWebServiceClient mnitClient,
      FilenameGenerator filenameGenerator,
      UploadedDocsPreparer uploadedDocsPreparer,
      ThreadPoolTaskExecutor threadPoolEexecutor) {
    this.xmlGenerator = xmlGenerator;
    this.pdfGenerator = pdfGenerator;
    this.monitoringService = monitoringService;
    this.routingDecisionService = routingDecisionService;
    this.applicationStatusRepository = applicationStatusRepository;
    this.mnitClient = mnitClient;
    this.filenameGenerator = filenameGenerator;
    this.uploadedDocsPreparer = uploadedDocsPreparer;
    this.executor = threadPoolEexecutor;
  }

  public void processCafAndCcap(Application application) {
    monitoringService.setApplicationId(application.getId());
    // Send the CAF, CCAP, and XML files in parallel
    List<Thread> threads = createThreadsForSendingThisApplication(application, application.getId());

    // Wait for everything to finish before returning
    threads.forEach(thread -> {
      try {
        thread.join();
      } catch (InterruptedException e) {
        log.error("Thread interrupted for application with id " + application.getId(), e);
      }
    });
  }

  @NotNull
  private List<Thread> createThreadsForSendingThisApplication(Application application, String id) {
    List<Thread> threads = Collections.synchronizedList(new ArrayList<>());
    Set<RoutingDestination> allRoutingDestinations = new HashSet<>();
    ApplicationData applicationData = application.getApplicationData();

    // Create threads for sending CAF & CCAP pdfs to each recipient
    DocumentListParser.parse(applicationData).forEach(doc -> {
      List<RoutingDestination> routingDestinations =
          routingDecisionService.getRoutingDestinations(applicationData, doc);

      // Keep track of routing destinations so that we know who to send the XML to later
      allRoutingDestinations.addAll(routingDestinations);

      for (RoutingDestination rd : routingDestinations) {
        ApplicationFile pdf = pdfGenerator.generate(id, doc, CASEWORKER, rd);
        Thread thread = executor.createThread(() -> sendOrSetToFailed(application, rd, pdf, doc));
        thread.start();
        threads.add(thread);
      }
    });

    // Create threads for sending the xml to each recipient who also received a PDF
    allRoutingDestinations.forEach(rd -> {
      ApplicationFile xml = xmlGenerator.generate(id, CAF, CASEWORKER);
      Thread thread = executor.createThread(() -> sendOrSetToFailed(application, rd, xml, XML));
      thread.start();
      threads.add(thread);
    });
    return threads;
  }

  
  public void processUploadedDocuments(Application application) {
    
    List<ApplicationFile> combinedUploadedFiles = uploadedDocsPreparer.prepare(
	        application.getApplicationData().getUploadedDocs(),
	        application);

    if (combinedUploadedFiles.isEmpty()
        || (combinedUploadedFiles.stream().allMatch(uDoc -> uDoc.getFileBytes() == null))) {
	      log.error(
	          "There was an issue processing and delivering uploaded documents. Reach out to client to upload again.");
	      applicationStatusRepository.createOrUpdateAllForDocumentType(application,
	          UNDELIVERABLE, UPLOADED_DOC);
	      return;
	    }

	    List<RoutingDestination> routingDestinations = routingDecisionService
	        .getRoutingDestinations(application.getApplicationData(), UPLOADED_DOC);
	    
	    FlowType flowType = application.getFlow();
	    for (RoutingDestination routingDestination : routingDestinations) {
	      boolean sendXMLToDakota = routingDestination.getName().equals(County.Dakota.name())
	          && (flowType == FlowType.LATER_DOCS || flowType == FlowType.HEALTHCARE_RENEWAL);

	      if (sendXMLToDakota) {
	        ApplicationFile xml = xmlGenerator.generate(application.getId(), XML, CASEWORKER);
	        sendOrSetToFailed(application, routingDestination, xml, XML);
	      }
	      log.info("Uploaded docs to submit %s".formatted(combinedUploadedFiles.size()));
	      for (int i = 0; i < combinedUploadedFiles.size(); i++) {
	         ApplicationFile uploadedDoc = combinedUploadedFiles.get(i);
	         // rename file with filename that is specific to this destination
	         String extension = Utils.getFileType(uploadedDoc.getFileName());
	         String newFilename = filenameGenerator.generateUploadedDocumentName(application, i,
	             extension, routingDestination, combinedUploadedFiles.size());
	         ApplicationFile renamedFile = new ApplicationFile(uploadedDoc.getFileBytes(),
	             newFilename);

	         sendOrSetToFailed(application, routingDestination, renamedFile, UPLOADED_DOC);
           } /*
              * String newFilename = filenameGenerator.generateCombinedUploadedDocsName(application,
              * "pdf", routingDestination); ApplicationFile renamedFile = new
              * ApplicationFile(combinedUploadedFiles.get(0).getFileBytes(), newFilename);
              * 
              * sendOrSetToFailed(application, routingDestination, renamedFile, UPLOADED_DOC);
              */
	    }
          
	    }


  private void sendOrSetToFailed(Application application, RoutingDestination routingDestination,
      ApplicationFile renamedFile, Document document) {
    try {
      applicationStatusRepository.createOrUpdate(application.getId(), document,
          routingDestination.getName(),
          SENDING, renamedFile.getFileName());
      mnitClient.send(application, renamedFile, routingDestination, document);
    } catch (Exception e) {
      applicationStatusRepository.createOrUpdate(application.getId(), document,
          routingDestination.getName(),
          DELIVERY_FAILED, renamedFile.getFileName());
      log.error("Failed to send document %s to recipient %s for application %s with error, "
          .formatted(document, routingDestination.getName(), application.getId()), e);
    }
  }

}