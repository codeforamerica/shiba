package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.getFileContentsAsByteArray;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UploadedDocsPreparerTest {

  private final PdfGenerator pdfGenerator = mock(PdfGenerator.class);
  private UploadedDocsPreparer uploadedDocsPreparer;
  private Application application;
  private byte[] coverPage;
  private ApplicationFile imageFile;
  private ApplicationFile wordDocFile;
  private ApplicationFile combinedPdfFile;
  private ApplicationFile testPdfFile;
  private UploadedDocument imageDoc;
  private UploadedDocument wordDocument;
  private UploadedDocument testPdfDoc;
  private ApplicationFile uploadedWordDocWithoutCoverPage;
  private ApplicationFile uploadedPdfWithoutCoverPage;
  private final String imageFilename = "shiba+file.jpg";
  private final String docxFilename = "testWord.docx";
  private final String combinedPdfFilename = "combined-pdf.pdf";
  private final String fileWithoutCoverPageFilename = "fileWithoutCoverPage.doc";
  private final String testPdfFilename = "shiba+file.pdf";
  private RoutingDestination routDest;
  

  @BeforeEach
  void setUp() throws IOException {
    uploadedDocsPreparer = new UploadedDocsPreparer(pdfGenerator);
    coverPage = getFileContentsAsByteArray("test-cover-pages.pdf");
    var image = getFileContentsAsByteArray(imageFilename);
    var wordDoc = getFileContentsAsByteArray(docxFilename);
    var combinePdf = getFileContentsAsByteArray(combinedPdfFilename);
    var testPdf = getFileContentsAsByteArray(testPdfFilename);
    imageFile = new ApplicationFile(image, imageFilename);
    wordDocFile = new ApplicationFile(wordDoc, docxFilename);
    combinedPdfFile = new ApplicationFile(combinePdf, combinedPdfFilename);
    testPdfFile = new ApplicationFile(testPdf, testPdfFilename);
    uploadedWordDocWithoutCoverPage = new ApplicationFile(wordDoc, fileWithoutCoverPageFilename);
    uploadedPdfWithoutCoverPage = new ApplicationFile(testPdf, testPdfFilename);
    imageDoc = new UploadedDocument(imageFilename, "", "", "",
        image.length);
    wordDocument = new UploadedDocument(docxFilename, "", "", "",
        wordDoc.length);
    testPdfDoc = new UploadedDocument(testPdfFilename,"","","",testPdf.length);
    ApplicationData applicationData = new ApplicationData();
    applicationData.setId("9870000123");
    applicationData.setUploadedDocs(List.of(imageDoc, wordDocument, testPdfDoc));
    applicationData.setFlow(FlowType.LATER_DOCS);
    application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .build();
    when(pdfGenerator.generateCoverPageForUploadedDocs(application)).thenReturn(coverPage);
  }

  @Test
  void prepareShouldReturnAListOfFileWithCoverPagesAttached() {
    
    when(pdfGenerator.generateCombinedUploadedDocument(List.of(imageDoc, wordDocument), application,
        coverPage)).thenReturn(List.of(combinedPdfFile));

    List<String> preparedDocNames = uploadedDocsPreparer
        .prepare(List.of(imageDoc, wordDocument), application)
        .stream().map(ApplicationFile::getFileName).collect(Collectors.toList());

    assertThat(preparedDocNames).isEqualTo(List.of(combinedPdfFilename));
  }

  @Test
  void prepareShouldAttachFilesWithoutCoverPagesIfNPEIsThrown() throws NoSuchMethodException, SecurityException {
    when(pdfGenerator.generateCombinedUploadedDocument(List.of(imageDoc,testPdfDoc), application, coverPage))
        .thenReturn(List.of(combinedPdfFile));
    
    
    when(pdfGenerator.generateCombinedUploadedDocument(List.of(imageDoc, testPdfDoc), application,
        coverPage, routDest)).thenThrow(new NullPointerException());
    
    when(pdfGenerator.generateCombinedUploadedDocument(List.of(imageDoc,testPdfDoc), application, coverPage))
    .thenReturn(List.of(imageFile, testPdfFile ));
   
    List<String> preparedDocNames = uploadedDocsPreparer
        .prepare(List.of(imageDoc, testPdfDoc), application)
        .stream().map(ApplicationFile::getFileName).collect(Collectors.toList());

    assertThat(preparedDocNames).isEqualTo(List.of(imageFilename,testPdfFilename));
  }
}