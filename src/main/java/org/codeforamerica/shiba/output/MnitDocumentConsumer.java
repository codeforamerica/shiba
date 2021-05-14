package org.codeforamerica.shiba.output;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Component
@Slf4j
public class MnitDocumentConsumer {
    private final MnitEsbWebServiceClient mnitClient;
    private final XmlGenerator xmlGenerator;
    private final PdfGenerator pdfGenerator;
    private final ApplicationDataParser<List<Document>> documentListParser;
    private final MonitoringService monitoringService;
    private final String activeProfile;
    private final ApplicationRepository applicationRepository;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                XmlGenerator xmlGenerator,
                                PdfGenerator pdfGenerator,
                                ApplicationDataParser<List<Document>> documentListParser,
                                MonitoringService monitoringService,
                                @Value("${spring.profiles.active:dev}") String activeProfile,
                                ApplicationRepository applicationRepository) {
        this.mnitClient = mnitClient;
        this.xmlGenerator = xmlGenerator;
        this.pdfGenerator = pdfGenerator;
        this.documentListParser = documentListParser;
        this.monitoringService = monitoringService;
        this.activeProfile = activeProfile;
        this.applicationRepository = applicationRepository;
    }

    public void process(Application application) {
        monitoringService.setApplicationId(application.getId());
        // Send the CAF and CCAP as PDFs
        documentListParser.parse(application.getApplicationData()).forEach(documentType -> mnitClient.send(
                pdfGenerator.generate(application.getId(), documentType, CASEWORKER), application.getCounty(),
                application.getId(), documentType)
        );
        application.getApplicationData().setStatus(Status.SENDING_APPLICATION);
        applicationRepository.save(application);

        boolean sentSuccessfully = mnitClient.send(xmlGenerator.generate(application.getId(), Document.CAF, CASEWORKER), application.getCounty(), application.getId(), Document.CAF);
        if (sentSuccessfully) {
            application.getApplicationData().setStatus(Status.SUBMITTED_APPLICATION);
            applicationRepository.save(application);
        } else {
            log.error("Failed to send application id={}", application.getId());
        }
    }

    public void processUploadedDocuments(Application application) {
        List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
        byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
        for (int i = 0; i < uploadedDocs.size(); i++) {
            UploadedDocument uploadedDocument = uploadedDocs.get(i);
            ApplicationFile fileToSend = pdfGenerator.generateForUploadedDocument(uploadedDocument, i, application, coverPage);

            if (fileToSend.getFileBytes().length > 0) {
                log.info("Now sending: " + fileToSend.getFileName() + " original filename: " + uploadedDocument.getFilename());
                if (application.getApplicationData().getStatus() != Status.SENDING_APPLICATION) {
                    application.getApplicationData().setStatus(Status.SENDING_DOCS);
                    applicationRepository.save(application);
                } else {
                    log.warn("Application has not yet successfully submitted id={}", application.getId());
                }

                boolean sentSuccessfully = mnitClient.send(fileToSend, application.getCounty(), application.getId(), UPLOADED_DOC);
                if (sentSuccessfully && application.getApplicationData().getStatus() != Status.SENDING_APPLICATION) {
                    application.getApplicationData().setStatus(Status.SUBMITTED_DOCS);
                    applicationRepository.save(application);
                } else {
                    log.error("There was an error sending uploaded documents for application status={}, id={}",
                            application.getApplicationData().getStatus(),
                            application.getId());
                }

                log.info("Finished sending document " + fileToSend.getFileName());
            } else if (activeProfile.equals("demo") || activeProfile.equals("staging") || activeProfile.equals("production")) {
                log.error("Skipped uploading file " + uploadedDocument.getFilename() + " because it was empty. This should only happen in a dev environment.");
            } else {
                log.info("Pretending to send file " + uploadedDocument.getFilename() + ".");
            }
        }
    }
}