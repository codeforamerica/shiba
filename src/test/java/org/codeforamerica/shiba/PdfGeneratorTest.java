package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PdfGeneratorTest {
    private final PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
    private final PdfFieldFiller pdfFiller = mock(PdfFieldFiller.class);
    private final PdfGenerator pdfGenerator = new PdfGenerator(pdfFieldMapper, pdfFiller);

    @BeforeEach
    void setUp() {
        when(pdfFieldMapper.map(any())).thenReturn(List.of());
        when(pdfFiller.fill(any())).thenReturn(new ApplicationFile("defaultBytes".getBytes(), "defaultFileName"));
    }

    @Test
    void shouldMapPdfFieldsFromFlattenedScreenInputs() {
        ApplicationInput input = new ApplicationInput(List.of("someValue"), "someName", ApplicationInputType.SINGLE_VALUE);
        pdfGenerator.generate(Map.of("screen1", List.of(input)));

        verify(pdfFieldMapper).map(Map.of("screen1", List.of(input)));
    }

    @Test
    void shouldFillThePdfWithItsFields() {
        List<PdfField> pdfFields = List.of(new SimplePdfField("some name", "some value"));
        when(pdfFieldMapper.map(any())).thenReturn(pdfFields);

        pdfGenerator.generate(Map.of());

        verify(pdfFiller).fill(pdfFields);
    }

    @Test
    void shouldReturnTheFilledPdf() {
        ApplicationFile expectedApplicationFile = new ApplicationFile("here is the pdf".getBytes(), "filename.pdf");
        when(pdfFiller.fill(any())).thenReturn(expectedApplicationFile);

        ApplicationFile actualApplicationFile = pdfGenerator.generate(Map.of());

        assertThat(actualApplicationFile).isEqualTo(expectedApplicationFile);
    }
}