package org.codeforamerica.shiba.output.pdf;

import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;

import com.itextpdf.pdfoffice.exceptions.PdfOfficeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.Utils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparers;
import org.codeforamerica.shiba.output.xml.FileGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PdfGenerator implements FileGenerator {

  private static final List<String> IMAGE_TYPES_TO_CONVERT_TO_PDF = List
      .of("jpg", "jpeg", "png", "gif");
  private static final List<String> DOC_TYPES_TO_CONVERT_TO_PDF = List
      .of("doc", "docx");
  private final PdfFieldMapper pdfFieldMapper;
  private final Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillerMap;
  private final Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillersMap;
  private final ApplicationRepository applicationRepository;
  private final DocumentRepository documentRepository;
  private final DocumentFieldPreparers preparers;
  private final FilenameGenerator fileNameGenerator;
  private final FileToPDFConverter pdfWordConverter;
  private final FeatureFlagConfiguration featureFlags;
  private final ServicingAgencyMap<CountyRoutingDestination> countyMap;

  public PdfGenerator(PdfFieldMapper pdfFieldMapper,
      Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers,
      Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillers,
      ApplicationRepository applicationRepository,
      DocumentRepository documentRepository,
      DocumentFieldPreparers preparers,
      FilenameGenerator fileNameGenerator,
      FileToPDFConverter pdfWordConverter,
      FeatureFlagConfiguration featureFlagConfiguration,
      ServicingAgencyMap<CountyRoutingDestination> countyMap
  ) {
    this.pdfFieldMapper = pdfFieldMapper;
    this.pdfFieldFillerMap = pdfFieldFillers;
    this.applicationRepository = applicationRepository;
    this.documentRepository = documentRepository;
    this.preparers = preparers;
    this.fileNameGenerator = fileNameGenerator;
    this.pdfWordConverter = pdfWordConverter;
    this.featureFlags = featureFlagConfiguration;
    this.pdfFieldWithCAFHHSuppFillersMap = pdfFieldWithCAFHHSuppFillers;
    this.countyMap = countyMap;
  }

  @Override
  public ApplicationFile generate(String applicationId, Document document, Recipient recipient) {
    Application application = applicationRepository.find(applicationId);
    return generate(application, document, recipient);
  }

  // Generates a pdf and gives it a filename corresponding to a specific routing destination
  public ApplicationFile generate(String applicationId, Document document, Recipient recipient,
      RoutingDestination routingDestination) {
    Application application = applicationRepository.find(applicationId);
    String filename = fileNameGenerator.generatePdfFilename(application,
        document, routingDestination);
    return generateWithFilename(application, document, recipient, filename);
  }

  public byte[] generateCoverPageForUploadedDocs(Application application) {
    return generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
  }

  public ApplicationFile generate(Application application, Document document, Recipient recipient) {
    String filename = fileNameGenerator.generatePdfFilename(application, document);
    return generateWithFilename(application, document, recipient, filename);
  }

  public ApplicationFile generate(Application application, Document document, Recipient recipient,
      RoutingDestination routingDestination) {
    String filename = fileNameGenerator.generatePdfFilename(application, document,
        routingDestination);
    return generateWithFilename(application, document, recipient, filename);
  }

  private ApplicationFile generateWithFilename(Application application, Document document,
      Recipient recipient, String filename) {
    List<DocumentField> documentFields = preparers.prepareDocumentFields(application, document,
        recipient);
    var houseHold = application.getApplicationData().getApplicantAndHouseholdMember();
    PdfFieldFiller pdfFiller = pdfFieldFillerMap.get(recipient).get(document);

      if (document.equals(Document.CAF) && (houseHold.size() > 5 && houseHold.size() <= 10)) {
        pdfFiller = pdfFieldWithCAFHHSuppFillersMap.get(recipient).get(document);

    }

    List<PdfField> fields = pdfFieldMapper.map(documentFields);
    return pdfFiller.fill(fields, application.getId(), filename);
  }

  public ApplicationFile generateForUploadedDocument(UploadedDocument uploadedDocument,
      int documentIndex, Application application, byte[] coverPage,
      RoutingDestination routingDest) {
    var fileBytes = documentRepository.get(uploadedDocument.getS3Filepath());
    if (fileBytes != null) {
      var extension = Utils.getFileType(uploadedDocument.getFilename());
      boolean flagIsNotNull =
          featureFlags != null && featureFlags.get("word-to-pdf") != null; //need this for tests
      if (flagIsNotNull && featureFlags.get("word-to-pdf").isOn()
          && DOC_TYPES_TO_CONVERT_TO_PDF.contains(extension)) {
        try {
          InputStream inputStream = new ByteArrayInputStream(fileBytes);
          fileBytes = pdfWordConverter.convertWordDocToPDFwithStreams(inputStream);
          extension = "pdf";
        } catch (PdfOfficeException | IOException e) {
          log.warn("failed to convert document " + uploadedDocument.getFilename()
              + " to pdf. Maintaining original type. " + e.getMessage());
        }

      } else if (IMAGE_TYPES_TO_CONVERT_TO_PDF.contains(extension)) {
        try {
          fileBytes = convertImageToPdf(fileBytes, uploadedDocument.getFilename());
          extension = "pdf";
        } catch (Exception e) {
          log.warn("failed to convert document " + uploadedDocument.getFilename()
              + " to pdf. Maintaining original type");
        }
      } else if (!extension.equals("pdf")) {
        log.warn("Unsupported file-type: " + extension);
      }

      if (extension.equals("pdf") && coverPage != null) {
        fileBytes = addCoverPageToPdf(coverPage, fileBytes);
      }
      String filename = uploadedDocument.getSysFileName();
      filename = (filename == null || (filename!=null && !filename.contains(routingDest.getDhsProviderId())))
          ? fileNameGenerator.generateUploadedDocumentName(application, documentIndex, extension,
          routingDest)
          : uploadedDocument.getSysFileName();
      return new ApplicationFile(fileBytes, filename);
    }
    return null;
  }

  public ApplicationFile generateForUploadedDocument(UploadedDocument uploadedDocument,
      int documentIndex, Application application, byte[] coverPage) {
    return generateForUploadedDocument(uploadedDocument, documentIndex, application, coverPage,
        countyMap.get(application.getCounty()));
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

  private byte[] convertImageToPdf(byte[] imageFileBytes, String filename) throws Exception {
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
    } catch (Exception e) {
      log.error(
          "convertImageToPdf Error for file " + filename + ". Error message: " + e.getMessage());
      throw e;
    }
  }
}
