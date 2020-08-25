package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PdfGeneratorTest {
    private final PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
    private final PdfFieldFiller pdfFiller = mock(PdfFieldFiller.class);
    private final PdfGenerator pdfGenerator = new PdfGenerator(pdfFieldMapper, pdfFiller);

    @BeforeEach
    void setUp() {
        when(pdfFieldMapper.map(any())).thenReturn(List.of());
        when(pdfFiller.fill(any(), any())).thenReturn(new ApplicationFile("defaultBytes".getBytes(), "defaultFileName"));
    }

    @Test
    void shouldMapPdfFieldsFromFlattenedScreenInputs() {
        ApplicationInput input = new ApplicationInput("screen1", "someName", List.of("someValue"), ApplicationInputType.SINGLE_VALUE);
        pdfGenerator.generate(List.of(input), "");

        verify(pdfFieldMapper).map(List.of(input));
    }

    @Test
    void shouldFillThePdfWithItsFields() {
        List<PdfField> pdfFields = List.of(new SimplePdfField("some name", "some value"));
        when(pdfFieldMapper.map(any())).thenReturn(pdfFields);

        String applicationId = "application-id";
        pdfGenerator.generate(List.of(), applicationId);

        verify(pdfFiller).fill(pdfFields, applicationId);
    }

    @Test
    void shouldReturnTheFilledPdf() {
        ApplicationFile expectedApplicationFile = new ApplicationFile("here is the pdf".getBytes(), "filename.pdf");
        when(pdfFiller.fill(any(), any())).thenReturn(expectedApplicationFile);

        ApplicationFile actualApplicationFile = pdfGenerator.generate(List.of(), "");

        assertThat(actualApplicationFile).isEqualTo(expectedApplicationFile);
    }
}