package org.codeforamerica.shiba.output.pdf;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class PDFBoxFieldFillerTest {

  private final PDFBoxFieldFiller PDFBoxFieldFiller = new PDFBoxFieldFiller(
      List.of(
          new ClassPathResource("test-cover-pages.pdf"),
          new ClassPathResource("test-caf.pdf")
      )
  );

  @Test
  void shouldMapTextFields() throws IOException {
    String expectedFieldValue = "Michael";
    Collection<PdfField> fields = List.of(
        new SimplePdfField("TEXT_FIELD", expectedFieldValue)
    );

    ApplicationFile applicationFile = PDFBoxFieldFiller.fill(fields, "", "");

    PDAcroForm acroForm = getPdAcroForm(applicationFile);

    assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo(expectedFieldValue);
  }

  @Test
  void shouldSetTheAppropriateNonValueForTheFieldType() throws IOException {
    Collection<PdfField> fields = List.of(
        new SimplePdfField("TEXT_FIELD", null)
    );

    ApplicationFile applicationFile = PDFBoxFieldFiller.fill(fields, "", "");

    PDAcroForm acroForm = getPdAcroForm(applicationFile);
    assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo("");
  }

  @Test
  void shouldMapMultipleChoiceFields() throws IOException {
    Collection<PdfField> fields = List.of(
        new BinaryPdfField("BINARY_FIELD_1"),
        new BinaryPdfField("BINARY_FIELD_3")
    );

    ApplicationFile applicationFile = PDFBoxFieldFiller.fill(fields, "", "");

    PDAcroForm acroForm = getPdAcroForm(applicationFile);
    assertThat(acroForm.getField("BINARY_FIELD_1").getValueAsString()).isEqualTo("Yes");
    assertThat(acroForm.getField("BINARY_FIELD_3").getValueAsString()).isEqualTo("Yes");
  }

  @Test
  void shouldReturnTheAppropriateFilename() {
    String applicationId = "applicationId";
    String fileName = "fileName.pdf";
    ApplicationFile applicationFile = PDFBoxFieldFiller.fill(emptyList(), applicationId, fileName);
    assertThat(applicationFile.getFileName()).isEqualTo(fileName);
  }

  @Test
  void shouldConcatenateAllResourcePDFs() throws IOException {
    ApplicationFile applicationFile = PDFBoxFieldFiller.fill(emptyList(), "id", "");

    Path path = Files.createTempDirectory("");
    File file = new File(path.toFile(), "test-caf.pdf");
    Files.write(file.toPath(), applicationFile.getFileBytes());
    PDDocument pdDocument = Loader.loadPDF(file);

    assertThat(pdDocument.getNumberOfPages()).isEqualTo(21);
  }

  @Test
  void shouldNotThrowException_whenFieldIsNotFound() {
    assertThatCode(() -> PDFBoxFieldFiller.fill(List.of(
        new SimplePdfField("definitely-not-a-field", "")), "id",
        "")).doesNotThrowAnyException();
  }

  @Test
  void shouldSupportEmojis() throws IOException {
    String submittedValue = "Michael😃";
    String expectedValue = "Michael😃";

    Collection<PdfField> fields = List.of(
        new SimplePdfField("TEXT_FIELD", submittedValue)
    );
    ApplicationFile applicationFile = PDFBoxFieldFiller.fill(fields, "", "");
    PDAcroForm acroForm = getPdAcroForm(applicationFile);

    assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo(expectedValue);
  }

  private PDAcroForm getPdAcroForm(ApplicationFile applicationFile) throws IOException {
    Path path = Files.createTempDirectory("");
    File file = new File(path.toFile(), "test-caf.pdf");
    Files.write(file.toPath(), applicationFile.getFileBytes());

    PDDocument pdDocument = Loader.loadPDF(file);
    return pdDocument.getDocumentCatalog().getAcroForm();
  }
}
