package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.getFileContentsAsByteArray;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UploadedDocsPreparerTest {

  private final PdfGenerator pdfGenerator = mock(PdfGenerator.class);
  private UploadedDocsPreparer uploadedDocsPreparer;

  @BeforeEach
  void setUp() {
    uploadedDocsPreparer = new UploadedDocsPreparer(pdfGenerator);
  }

  @Test
  void prepareShouldReturnAListOfFileWithCoverPagesAttached() throws IOException {
    String imageFilename = "shiba+file.jpg";
    String docxFilename = "testWord.docx";
    var image = getFileContentsAsByteArray(imageFilename);
    var wordDoc = getFileContentsAsByteArray(docxFilename);
    var coverPage = getFileContentsAsByteArray("test-cover-pages.pdf");
    ApplicationFile imageFile = new ApplicationFile(image, imageFilename);
    ApplicationFile wordDocFile = new ApplicationFile(wordDoc, docxFilename);
    UploadedDocument uploadedImageDoc = new UploadedDocument(imageFilename, "", "", "",
        image.length);
    UploadedDocument uploadedWordDoc = new UploadedDocument(docxFilename, "", "", "",
        wordDoc.length);
    ApplicationData applicationData = new ApplicationData();
    applicationData.setId("9870000123");
    applicationData.setUploadedDocs(List.of(uploadedImageDoc, uploadedWordDoc));
    applicationData.setFlow(FlowType.LATER_DOCS);
    Application application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .build();
    when(pdfGenerator.generateCoverPageForUploadedDocs(application)).thenReturn(coverPage);
    when(pdfGenerator.generateForUploadedDocument(uploadedImageDoc, 0, application,
        coverPage)).thenReturn(imageFile);
    when(pdfGenerator.generateForUploadedDocument(uploadedWordDoc, 1, application,
        coverPage)).thenReturn(wordDocFile);

    List<String> preparedDocNames = uploadedDocsPreparer
        .prepare(List.of(uploadedImageDoc, uploadedWordDoc), application)
        .stream().map(ApplicationFile::getFileName).collect(Collectors.toList());

    assertThat(preparedDocNames).isEqualTo(List.of(imageFilename, docxFilename));
  }
}