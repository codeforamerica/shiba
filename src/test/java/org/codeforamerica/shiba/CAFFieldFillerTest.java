package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.MaritalStatus.LEGALLY_SEPARATED;
import static org.codeforamerica.shiba.PdfFieldMapper.*;

class CAFFieldFillerTest {

    private final CAFFieldFiller subject = new CAFFieldFiller();

    @Test
    void shouldMapTextFields() throws IOException {
        String expectedField1 = "Michael";
        String expectedField2 = "Scott";
        Collection<PDFField> fields = List.of(
                new SimplePDFField(APPLICANT_FIRST_NAME, expectedField1),
                new SimplePDFField(APPLICANT_LAST_NAME, expectedField2)
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField(APPLICANT_FIRST_NAME).getValueAsString()).isEqualTo(expectedField1);
        assertThat(acroForm.getField(APPLICANT_LAST_NAME).getValueAsString()).isEqualTo(expectedField2);
    }

    @Test
    void shouldMapDateFields() throws IOException {
        Collection<PDFField> fields = List.of(
                new DatePDFField(DATE_OF_BIRTH, LocalDate.of(2020, 1, 31))
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField(DATE_OF_BIRTH).getValueAsString()).isEqualTo("01/31/2020");
    }

    @Test
    void shouldMapToggleFields() throws IOException {
        Collection<PDFField> fields = List.of(new TogglePDFField(NEED_INTERPRETER, true));
        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField(NEED_INTERPRETER).getValueAsString()).isEqualTo("LEFT");
    }

    @Test
    void shouldMapRadioFields() throws IOException {
        Collection<PDFField> fields = List.of(new SimplePDFField(MARITAL_STATUS, LEGALLY_SEPARATED.toString()));

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);

        assertThat(acroForm.getField(MARITAL_STATUS).getValueAsString()).isEqualTo(LEGALLY_SEPARATED.toString());
    }

    @Test
    void shouldSetTheAppropriateNonValueForTheFieldType() throws IOException {
        Collection<PDFField> fields = List.of(
                new SimplePDFField(APPLICANT_FIRST_NAME, null),
                new TogglePDFField(NEED_INTERPRETER, null)
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);
        assertThat(acroForm.getField(APPLICANT_FIRST_NAME).getValueAsString()).isEqualTo("");
        assertThat(acroForm.getField(NEED_INTERPRETER).getValueAsString()).isEqualTo("Off");
    }

    @Test
    void shouldMapMultipleChoiceFields() throws IOException {
        Collection<PDFField> fields = List.of(
                new BinaryPDFField(FOOD),
                new BinaryPDFField(EMERGENCY)
        );

        PdfFile pdfFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(pdfFile);
        assertThat(acroForm.getField(FOOD).getValueAsString()).isEqualTo("Yes");
        assertThat(acroForm.getField(CASH).getValueAsString()).isEqualTo("Off");
        assertThat(acroForm.getField(EMERGENCY).getValueAsString()).isEqualTo("Yes");
    }

    @Test
    void shouldReturnTheAppropriateFilename() {
        PdfFile pdfFile = subject.fill(emptyList());

        assertThat(pdfFile.getFileName()).isEqualTo("DHS-5223.pdf");
    }

    private PDAcroForm getPdAcroForm(PdfFile pdfFile) throws IOException {
        Path path = Files.createTempDirectory("");
        File file = new File(path.toFile(), "test.pdf");
        Files.write(file.toPath(), pdfFile.getFileBytes());

        PDDocument pdDocument = PDDocument.load(file);
        return pdDocument.getDocumentCatalog().getAcroForm();
    }
}