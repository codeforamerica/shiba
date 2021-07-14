package org.codeforamerica.shiba.output;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.ApplicationStatusUpdater;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.*;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Component
@Slf4j
public class MnitDocumentConsumer {
    private final MnitEsbWebServiceClient mnitClient;
    private final XmlGenerator xmlGenerator;
    private final PdfGenerator pdfGenerator;
    private final MonitoringService monitoringService;
    private final String activeProfile;
    private final ApplicationStatusUpdater applicationStatusUpdater;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                XmlGenerator xmlGenerator,
                                PdfGenerator pdfGenerator,
                                MonitoringService monitoringService,
                                @Value("${spring.profiles.active:dev}") String activeProfile,
                                ApplicationStatusUpdater applicationStatusUpdater) {
        this.mnitClient = mnitClient;
        this.xmlGenerator = xmlGenerator;
        this.pdfGenerator = pdfGenerator;
        this.monitoringService = monitoringService;
        this.activeProfile = activeProfile;
        this.applicationStatusUpdater = applicationStatusUpdater;
    }

    public void process(Application application) {
        monitoringService.setApplicationId(application.getId());
        // Send the CAF and CCAP as PDFs
        DocumentListParser.parse(application.getApplicationData()).forEach(documentType -> {
            try {
                updateDocumentStatus(documentType, application.getId(), SENDING);
                mnitClient.send(pdfGenerator.generate(application.getId(), documentType, CASEWORKER), application.getCounty(), application.getId(), documentType, application.getFlow());
            } catch (Exception e) {
                updateDocumentStatus(documentType, application.getId(), DELIVERY_FAILED);
                log.error("Failed to send with error, ", e);
            }
        });
        mnitClient.send(xmlGenerator.generate(application.getId(), CAF, CASEWORKER), application.getCounty(), application.getId(), CAF, application.getFlow());
    }

    public void processUploadedDocuments(Application application) {
        applicationStatusUpdater.updateUploadedDocumentsStatus(application.getId(), UPLOADED_DOC, SENDING);
        List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
        byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
        for (int i = 0; i < uploadedDocs.size(); i++) {
            UploadedDocument uploadedDocument = uploadedDocs.get(i);
            ApplicationFile fileToSend = pdfGenerator.generateForUploadedDocument(uploadedDocument, i, application, coverPage);

            if (fileToSend.getFileBytes().length > 0) {
                log.info("Now sending: " + fileToSend.getFileName() + " original filename: " + uploadedDocument.getFilename());
                mnitClient.send(fileToSend, application.getCounty(), application.getId(), UPLOADED_DOC, application.getFlow());
                log.info("Finished sending document " + fileToSend.getFileName());
            } else if ("demo".equals(activeProfile) || "staging".equals(activeProfile) || "production".equals(activeProfile)) {
                log.error("Skipped uploading file " + uploadedDocument.getFilename() + " because it was empty. This should only happen in a dev environment.");
            } else {
                log.info("Pretending to send file " + uploadedDocument.getFilename() + ".");
            }
        }
    }

    public void updateDocumentStatus(Document documentType, String id, Status status) {
        if (documentType == CCAP) {
            applicationStatusUpdater.updateCcapApplicationStatus(id, CCAP, status);
        }
        if (documentType == CAF) {
            applicationStatusUpdater.updateCafApplicationStatus(id, CAF, status);
        }
    }
}