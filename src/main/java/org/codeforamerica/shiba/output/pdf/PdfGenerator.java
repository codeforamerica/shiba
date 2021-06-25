package org.codeforamerica.shiba.output.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.codeforamerica.shiba.Utils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.documents.CombinedAzureS3DocumentRepositoryService;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.output.xml.FileGenerator;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PdfGenerator implements FileGenerator {
    private final PdfFieldMapper pdfFieldMapper;
    private final Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillerMap;
    private final ApplicationRepository applicationRepository;
    private final CombinedAzureS3DocumentRepositoryService combinedAzureS3DocumentRepositoryService;
//    private final DocumentRepositoryService documentRepositoryService;
    private final ApplicationInputsMappers mappers;
    private final FileNameGenerator fileNameGenerator;

    private static final List<String> IMAGE_TYPES_TO_CONVERT_TO_PDF = List.of("jpg", "jpeg", "png", "gif");

    public PdfGenerator(PdfFieldMapper pdfFieldMapper,
                        Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers,
                        ApplicationRepository applicationRepository,
//                        DocumentRepositoryService documentRepositoryService,
                        CombinedAzureS3DocumentRepositoryService combinedAzureS3DocumentRepositoryService,
                        ApplicationInputsMappers mappers,
                        FileNameGenerator fileNameGenerator
    ) {
        this.pdfFieldMapper = pdfFieldMapper;
        this.pdfFieldFillerMap = pdfFieldFillers;
//        this.documentRepositoryService = documentRepositoryService;
        this.applicationRepository = applicationRepository;
        this.combinedAzureS3DocumentRepositoryService = combinedAzureS3DocumentRepositoryService;
        this.mappers = mappers;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    public ApplicationFile generate(String applicationId, Document document, Recipient recipient) {
        Application application = applicationRepository.find(applicationId);
        return generate(application, document, recipient);
    }

    public ApplicationFile generate(Application application, Document document, Recipient recipient) {
        List<ApplicationInput> applicationInputs = mappers.map(application, document, recipient);
        PdfFieldFiller pdfFiller = pdfFieldFillerMap.get(recipient).get(document);
        return pdfFiller.fill(pdfFieldMapper.map(applicationInputs), application.getId(), fileNameGenerator.generatePdfFileName(application, document));
    }

    public ApplicationFile generateForUploadedDocument(UploadedDocument uploadedDocument, int documentIndex, Application application, byte[] coverPage) {
        var fileBytes = combinedAzureS3DocumentRepositoryService.getFromAzureWithFallbackToS3(uploadedDocument.getS3Filepath());
        if(fileBytes != null) {
            var extension = Utils.getFileType(uploadedDocument.getFilename());
            if (IMAGE_TYPES_TO_CONVERT_TO_PDF.contains(extension)) {
                try {
                    fileBytes = convertImageToPdf(fileBytes, uploadedDocument.getFilename());
                    extension = "pdf";
                } catch (IOException e) {
                    log.error("failed to convert document " + uploadedDocument.getFilename() + " to pdf. Maintaining original type");
                }
            }

            if (extension.equals("pdf")) {
                fileBytes = addCoverPageToPdf(coverPage, fileBytes);
            }

            String filename = fileNameGenerator.generateUploadedDocumentName(application, documentIndex, extension);
            return new ApplicationFile(fileBytes, filename);
        }
         return null;
    }

    private byte[] addCoverPageToPdf(byte[] coverPage, byte[] fileBytes) {
        PDFMergerUtility merger = new PDFMergerUtility();
        try (PDDocument coverPageDoc = PDDocument.load(coverPage);
             PDDocument uploadedDoc = PDDocument.load(fileBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            merger.appendDocument(coverPageDoc, uploadedDoc);
            coverPageDoc.save(outputStream);
            fileBytes = outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileBytes;
    }

    private byte[] convertImageToPdf(byte[] imageFileBytes, String filename) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var image = PDImageXObject.createFromByteArray(doc, imageFileBytes, filename);
            // Figure out page size
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

            var imagePage = new PDPage(pageSize);
            // Add a page of the right size to the PDF
            doc.addPage(imagePage);

            // Write the image into the PDF
            try (PDPageContentStream pdfContents = new PDPageContentStream(doc, imagePage)) {
                pdfContents.drawImage(image, x, y, scaledWidth, scaledHeight);
            }

            // put the doc in a byte array
            doc.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
