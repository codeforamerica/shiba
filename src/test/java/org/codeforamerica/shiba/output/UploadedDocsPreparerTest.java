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
  private UploadedDocument imageDoc;
  private UploadedDocument wordDocument;
  private ApplicationFile uploadedWordDocWithoutCoverPage;
  private final String imageFilename = "shiba+file.jpg";
  private final String docxFilename = "testWord.docx";
  private final String fileWithoutCoverPageFilename = "fileWithoutCoverPage.doc";

  @BeforeEach
  void setUp() throws IOException {
    uploadedDocsPreparer = new UploadedDocsPreparer(pdfGenerator);
    coverPage = getFileContentsAsByteArray("test-cover-pages.pdf");
    var image = getFileContentsAsByteArray(imageFilename);
    var wordDoc = getFileContentsAsByteArray(docxFilename);
    imageFile = new ApplicationFile(image, imageFilename);
    wordDocFile = new ApplicationFile(wordDoc, docxFilename);
    uploadedWordDocWithoutCoverPage = new ApplicationFile(wordDoc, fileWithoutCoverPageFilename);
    imageDoc = new UploadedDocument(imageFilename, "", "", "",
        image.length);
    wordDocument = new UploadedDocument(docxFilename, "", "", "",
        wordDoc.length);
    ApplicationData applicationData = new ApplicationData();
    applicationData.setId("9870000123");
    applicationData.setUploadedDocs(List.of(imageDoc, wordDocument));
    applicationData.setFlow(FlowType.LATER_DOCS);
    application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .build();
    when(pdfGenerator.generateCoverPageForUploadedDocs(application)).thenReturn(coverPage);
  }

  @Test
  void prepareShouldReturnAListOfFileWithCoverPagesAttached() {
    when(pdfGenerator.generateForUploadedDocument(imageDoc, 0, application,
        coverPage)).thenReturn(imageFile);
    when(pdfGenerator.generateForUploadedDocument(wordDocument, 1, application,
        coverPage)).thenReturn(wordDocFile);

    List<String> preparedDocNames = uploadedDocsPreparer
        .prepare(List.of(imageDoc, wordDocument), application)
        .stream().map(ApplicationFile::getFileName).collect(Collectors.toList());

    assertThat(preparedDocNames).isEqualTo(List.of(imageFilename, docxFilename));
  }

  @Test
  void prepareShouldAttachFilesWithoutCoverPagesIfNPEIsThrown() {
    when(pdfGenerator.generateForUploadedDocument(imageDoc, 0, application, coverPage))
        .thenReturn(imageFile);
    when(pdfGenerator.generateForUploadedDocument(wordDocument, 1, application,
        coverPage)).thenThrow(new NullPointerException());
    when(pdfGenerator.generateForUploadedDocument(wordDocument, 1, application,
        null)).thenReturn(uploadedWordDocWithoutCoverPage);

    List<String> preparedDocNames = uploadedDocsPreparer
        .prepare(List.of(imageDoc, wordDocument), application)
        .stream().map(ApplicationFile::getFileName).collect(Collectors.toList());

    assertThat(preparedDocNames).isEqualTo(List.of(imageFilename, fileWithoutCoverPageFilename));
  }
}