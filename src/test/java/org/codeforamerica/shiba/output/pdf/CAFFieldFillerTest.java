package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CAFFieldFillerTest {

    private Clock clock = mock(Clock.class);
    private final CAFFieldFiller subject = new CAFFieldFiller(
            new ClassPathResource("test.pdf"),
            clock);

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void shouldIncludeCreatedDate() throws IOException {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(154352134));

        ApplicationFile applicationFile = subject.fill(List.of());

        PDAcroForm acroForm = getPdAcroForm(applicationFile);

        assertThat(acroForm.getField("CREATED_DATE").getValueAsString())
                .isEqualTo("1974-11-22");
    }

    @Test
    void shouldMapTextFields() throws IOException {
        String expectedFieldValue = "Michael";
        Collection<PdfField> fields = List.of(
                new SimplePdfField("TEXT_FIELD", expectedFieldValue)
        );

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);

        assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo(expectedFieldValue);
    }

    @Test
    void shouldSetTheAppropriateNonValueForTheFieldType() throws IOException {
        Collection<PdfField> fields = List.of(
                new SimplePdfField("TEXT_FIELD", null)
        );

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);
        assertThat(acroForm.getField("TEXT_FIELD").getValueAsString()).isEqualTo("");
    }

    @Test
    void shouldMapMultipleChoiceFields() throws IOException {
        Collection<PdfField> fields = List.of(
                new BinaryPdfField("BINARY_FIELD_1"),
                new BinaryPdfField("BINARY_FIELD_3")
        );

        ApplicationFile applicationFile = subject.fill(fields);

        PDAcroForm acroForm = getPdAcroForm(applicationFile);
        assertThat(acroForm.getField("BINARY_FIELD_1").getValueAsString()).isEqualTo("Yes");
        assertThat(acroForm.getField("BINARY_FIELD_3").getValueAsString()).isEqualTo("Yes");
    }

    @Test
    void shouldReturnTheAppropriateFilename() {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(43215321L));

        ApplicationFile applicationFile = subject.fill(emptyList());

        assertThat(applicationFile.getFileName()).isEqualTo("43215321-test.pdf");
    }

    private PDAcroForm getPdAcroForm(ApplicationFile applicationFile) throws IOException {
        Path path = Files.createTempDirectory("");
        File file = new File(path.toFile(), "test.pdf");
        Files.write(file.toPath(), applicationFile.getFileBytes());

        PDDocument pdDocument = PDDocument.load(file);
        return pdDocument.getDocumentCatalog().getAcroForm();
    }
}