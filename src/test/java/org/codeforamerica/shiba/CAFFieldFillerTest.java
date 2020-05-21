package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class CAFFieldFillerTest {

    private final CAFFieldFiller subject = new CAFFieldFiller(new ClassPathResource("test.pdf"));

    @Test
    void shouldMapTextFields() throws IOException {
        String expectedFieldValue = "Michael";
        Collection<PDFField> fields = List.of(
                new SimplePDFField("TEXT_FIELD", expectedFieldValue)
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo(expectedFieldValue);
    }

    @Test
    void shouldMapDateFields() throws IOException {
        Collection<PDFField> fields = List.of(
                new DatePDFField("DATE_FIELD", LocalDate.of(2020, 1, 31))
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField("DATE_FIELD").getValueAsString()).isEqualTo("01/31/2020");
    }

    @Test
    void shouldMapToggleFields() throws IOException {
        Collection<PDFField> fields = List.of(new TogglePDFField("TOGGLE_FIELD", true));
        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField("TOGGLE_FIELD").getValueAsString()).isEqualTo("LEFT");
    }

    @Test
    void shouldMapRadioFields() throws IOException {
        Collection<PDFField> fields = List.of(new SimplePDFField("RADIO_FIELD", "RADIO_OPTION_1"));

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField("RADIO_FIELD").getValueAsString()).isEqualTo("RADIO_OPTION_1");
    }

    @Test
    void shouldSetTheAppropriateNonValueForTheFieldType() throws IOException {
        Collection<PDFField> fields = List.of(
                new SimplePDFField("TEXT_FIELD", null),
                new TogglePDFField("TOGGLE_FIELD", null)
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);
        assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo("");
        assertThat(acroForm.getField("TOGGLE_FIELD").getValueAsString()).isEqualTo("Off");
    }

    @Test
    void shouldMapMultipleChoiceFields() throws IOException {
        Collection<PDFField> fields = List.of(
                new BinaryPDFField("BINARY_FIELD_1", true),
                new BinaryPDFField("BINARY_FIELD_2", false),
                new BinaryPDFField("BINARY_FIELD_3", true)
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);
        assertThat(acroForm.getField("BINARY_FIELD_1").getValueAsString()).isEqualTo("Yes");
        assertThat(acroForm.getField("BINARY_FIELD_2").getValueAsString()).isEqualTo("Off");
        assertThat(acroForm.getField("BINARY_FIELD_3").getValueAsString()).isEqualTo("Yes");
    }

    @Test
    void shouldReturnTheAppropriateFilename() {
        PdfFile pdfFile = subject.fill(emptyList());

        assertThat(pdfFile.getFileName()).isEqualTo("test.pdf");
    }

    private PDAcroForm getPdAcroForm(PdfFile pdfFile) throws IOException {
        Path path = Files.createTempDirectory("");
        File file = new File(path.toFile(), "test.pdf");
        Files.write(file.toPath(), pdfFile.getFileBytes());

        PDDocument pdDocument = PDDocument.load(file);
        return pdDocument.getDocumentCatalog().getAcroForm();
    }
}