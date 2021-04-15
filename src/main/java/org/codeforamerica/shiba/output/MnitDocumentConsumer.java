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
import org.codeforamerica.shiba.documents.*;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
        List<String> acceptedImageTypes = List.of("jpg","jpeg");

        for (int i = 0; i < uploadedDocs.size(); i++) {
            UploadedDocument uploadedDocument = uploadedDocs.get(i);
            byte[] fileBytes = documentRepositoryService.get(uploadedDocument.getS3Filepath());
            String extension = Utils.getFileType(uploadedDocument.getFilename());

            if(acceptedImageTypes.contains(extension)){
                try {
                    PDDocument doc = new PDDocument();
                    PDImageXObject image = PDImageXObject.createFromByteArray(doc,fileBytes,uploadedDocument.getFilename());
                    PDRectangle pageSize = PDRectangle.LETTER;
                    int originalWidth = image.getWidth();
                    int originalHeight = image.getHeight();
                    float pageWidth = pageSize.getWidth();
                    float pageHeight = pageSize.getHeight();
                    float ratio = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
                    float scaledWidth = originalWidth * ratio;
                    float scaledHeight = originalHeight * ratio;
                    float x = (pageWidth - scaledWidth) / 2;
                    float y = (pageHeight - scaledHeight) / 2;
                    PDPage page = new PDPage(pageSize);
                    doc.addPage(page);

                    try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                        contents.drawImage(image, x, y, scaledWidth, scaledHeight);
                    }
                    doc.save("shiba.pdf");
                } catch (Exception e) {

                }
            }

            /*//String filename = fileNameGenerator.generateUploadedDocumentName(application, i, uploadedDocument.getFilename());
            String filename = "shiba_testing.pdf";
            ApplicationFile fileToSend = new ApplicationFile(fileBytes, filename);


            if (fileBytes.length > 0) {
                log.info("Now sending: " + filename + " original filename: " + uploadedDocument.getFilename());
                mnitClient.send(fileToSend, application.getCounty(), application.getId(), null);
                log.info("Finished sending document " + filename);
            } else if (activeProfile.equals("demo") || activeProfile.equals("staging") || activeProfile.equals("production")) {
                log.error("Skipped uploading file " + uploadedDocument.getFilename() + " because it was empty. This should only happen in a dev environment.");
            } else {
                log.info("Pretending to send file " + uploadedDocument.getFilename() + ".");
            }*/
        }
    }
}