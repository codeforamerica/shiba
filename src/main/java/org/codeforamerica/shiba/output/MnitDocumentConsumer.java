package org.codeforamerica.shiba.output;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.Utils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

@Component
@Slf4j
public class MnitDocumentConsumer {
    private final MnitEsbWebServiceClient mnitClient;
    private final XmlGenerator xmlGenerator;
    private final PdfGenerator pdfGenerator;
    private final ApplicationDataParser<List<Document>> documentListParser;
    private final MonitoringService monitoringService;
    private final DocumentRepositoryService documentRepositoryService;
    private final FileNameGenerator fileNameGenerator;
    private final String activeProfile;

    public MnitDocumentConsumer(MnitEsbWebServiceClient mnitClient,
                                XmlGenerator xmlGenerator,
                                PdfGenerator pdfGenerator,
                                ApplicationDataParser<List<Document>> documentListParser,
                                MonitoringService monitoringService,
                                DocumentRepositoryService documentRepositoryService,
                                FileNameGenerator fileNameGenerator,
                                @Value("${spring.profiles.active:dev}") String activeProfile) {
        this.mnitClient = mnitClient;
        this.xmlGenerator = xmlGenerator;
        this.pdfGenerator = pdfGenerator;
        this.documentListParser = documentListParser;
        this.monitoringService = monitoringService;
        this.documentRepositoryService = documentRepositoryService;
        this.fileNameGenerator = fileNameGenerator;
        this.activeProfile = activeProfile;
    }

    public void process(Application application) {
        monitoringService.setApplicationId(application.getId());
        // Send the CAF and CCAP as PDFs
        documentListParser.parse(application.getApplicationData()).forEach(documentType -> mnitClient.send(
                pdfGenerator.generate(application.getId(), documentType, CASEWORKER), application.getCounty(),
                application.getId(), documentType)
        );
        mnitClient.send(xmlGenerator.generate(application.getId(), Document.CAF, CASEWORKER), application.getCounty(), application.getId(), Document.CAF);
    }

    public void processUploadedDocuments(Application application) {
        List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
        List<String> imageTypesToConvertToPdf = List.of("jpg", "jpeg", "png", "gif");

        for (int i = 0; i < uploadedDocs.size(); i++) {
            UploadedDocument uploadedDocument = uploadedDocs.get(i);

            var fileBytes = documentRepositoryService.get(uploadedDocument.getS3Filepath());
            var extension = Utils.getFileType(uploadedDocument.getFilename());
            if (imageTypesToConvertToPdf.contains(extension)) {
                try {
                    fileBytes = convertImageToPdf(uploadedDocument, fileBytes);
                    extension = "pdf";
                } catch (IOException e) {
                    log.error("failed to convert document " + uploadedDocument.getFilename() + " to pdf. Maintaining original type");
                }
            }
            String filename = fileNameGenerator.generateUploadedDocumentName(application, i, extension);
            ApplicationFile fileToSend = new ApplicationFile(fileBytes, filename);

            if (fileBytes.length > 0) {
                log.info("Now sending: " + filename + " original filename: " + uploadedDocument.getFilename());
                mnitClient.send(fileToSend, application.getCounty(), application.getId(), null);
                log.info("Finished sending document " + filename);
            } else if (activeProfile.equals("demo") || activeProfile.equals("staging") || activeProfile.equals("production")) {
                log.error("Skipped uploading file " + uploadedDocument.getFilename() + " because it was empty. This should only happen in a dev environment.");
            } else {
                log.info("Pretending to send file " + uploadedDocument.getFilename() + ".");
            }
        }
    }

    private byte[] convertImageToPdf(UploadedDocument uploadedDocument, byte[] imageFileBytes) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var image = PDImageXObject.createFromByteArray(doc, imageFileBytes, uploadedDocument.getFilename());
            var pageSize = PDRectangle.LETTER;
            var originalWidth = image.getWidth();
            var originalHeight = image.getHeight();
            var pageWidth = pageSize.getWidth();
            var pageHeight = pageSize.getHeight();
            var ratio = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
            var scaledWidth = originalWidth * ratio;
            var scaledHeight = originalHeight * ratio;
            var x = (pageWidth - scaledWidth) / 2;
            var y = (pageHeight - scaledHeight) / 2;
            var page = new PDPage(pageSize);
            doc.addPage(page);

            try (PDPageContentStream pdfContents = new PDPageContentStream(doc, page)) {
                pdfContents.drawImage(image, x, y, scaledWidth, scaledHeight);
            }

            doc.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}