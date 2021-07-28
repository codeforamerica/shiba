package org.codeforamerica.shiba;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.documents.CombinedDocumentRepositoryService;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Service
@Slf4j
public class ResubmissionService {
    private final ApplicationRepository applicationRepository;
    private final MailGunEmailClient emailClient;
    private final CountyMap<MnitCountyInformation> countyMap;
    private final PdfGenerator pdfGenerator;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ResubmissionService(ApplicationRepository applicationRepository, MailGunEmailClient emailClient, CountyMap<MnitCountyInformation> countyMap, PdfGenerator pdfGenerator, CombinedDocumentRepositoryService combinedDocumentRepositoryService) {
        this.applicationRepository = applicationRepository;
        this.emailClient = emailClient;
        this.countyMap = countyMap;
        this.pdfGenerator = pdfGenerator;
    }

    //@Scheduled(fixedDelayString = "${resubmission.interval.milliseconds}")
    public void resubmitFailedApplications() {
        log.info("Resubmitting applications that failed to send");
        Map<Document, List<String>> documentsToIds = applicationRepository.getApplicationIdsToResubmit();

        documentsToIds.forEach((document, applicationIds) -> applicationIds.forEach(id -> {
            MDC.put("applicationId", id);
            log.info("Resubmitting " + document.name() + "(s) for application id " + id);
            Application application = applicationRepository.find(id);
            var countyEmail = countyMap.get(application.getCounty()).getEmail();
            try {
                if (document.equals(UPLOADED_DOC)) {
                    resubmitUploadedDocumentsForApplication(document, application, countyEmail);
                } else {
                    var applicationFile = pdfGenerator.generate(application, document, CASEWORKER);
                    emailClient.resubmitFailedEmail(countyEmail, document, applicationFile, application, Locale.ENGLISH);
                }
                applicationRepository.updateStatus(id, document, DELIVERED);
                log.info("Resubmitted " + document.name() + "(s) for application id " + id);
            } catch (Exception e) {
                log.error("Failed to resubmit application " + id + " via email");
                applicationRepository.updateStatus(id, document, RESUBMISSION_FAILED);
            }
        }));
    }

    private void resubmitUploadedDocumentsForApplication(Document document, Application application, String countyEmail) {
        var coverPage = pdfGenerator.generate(application, document, CASEWORKER).getFileBytes();
        var uploadedDocs = application.getApplicationData().getUploadedDocs();
        for (int i = 0; i < uploadedDocs.size(); i++) {
            UploadedDocument uploadedDocument = uploadedDocs.get(i);
            ApplicationFile fileToSend = pdfGenerator.generateForUploadedDocument(uploadedDocument, i, application, coverPage);
            var esbFilename = fileToSend.getFileName();
            var originalFilename = uploadedDocument.getFilename();
            log.info("Resubmitting uploaded doc: %s original filename: %s".formatted(esbFilename, originalFilename));
            emailClient.resubmitFailedEmail(countyEmail, document, fileToSend, application, Locale.ENGLISH);
            log.info("Finished resubmitting document %s".formatted(esbFilename));
        }
    }
}
