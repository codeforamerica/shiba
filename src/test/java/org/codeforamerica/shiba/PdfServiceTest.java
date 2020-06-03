package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PdfServiceTest {
    private final PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
    private final PdfFieldFiller pdfFiller = mock(PdfFieldFiller.class);
    private final PdfService pdfService = new PdfService(pdfFieldMapper, pdfFiller);

    @BeforeEach
    void setUp() {
        when(pdfFieldMapper.map(any())).thenReturn(List.of());
        when(pdfFiller.fill(any())).thenReturn(new ApplicationFile("defaultBytes".getBytes(), "defaultFileName"));
    }

    @Test
    void shouldMapPdfFieldsFromFlattenedScreenInputs() {
        Screens screens = new Screens();
        Form form1 = new Form();

        FormInput input1 = new FormInput();
        input1.name = "input 1";
        input1.type = FormInputType.TEXT;

        FormInput input2 = new FormInput();
        input2.name = "input 2";
        input2.type = FormInputType.TEXT;
        input2.setType(FormInputType.INPUT_WITH_FOLLOW_UP);

        FormInputWithFollowUps inputWithFollowUps = new FormInputWithFollowUps();
        inputWithFollowUps.name = "inputWithFollowUps";
        inputWithFollowUps.type = FormInputType.TEXT;

        FormInput input3 = new FormInput();
        input3.name = "input 3";
        input3.type = FormInputType.TEXT;

        inputWithFollowUps.setFollowUps(List.of(input3));
        input2.setInputWithFollowUps(inputWithFollowUps);
        form1.setInputs(List.of(input1, input2));
        screens.put("screen1", form1);

        pdfService.generatePdf(screens);

        verify(pdfFieldMapper).map(Map.of(
                "screen1", List.of(input1, inputWithFollowUps, input3)
        ));
    }

    @Test
    void shouldFillThePdfWithItsFields() {
        List<PdfField> pdfFields = List.of(new SimplePdfField("some name", "some value"));
        when(pdfFieldMapper.map(any())).thenReturn(pdfFields);
        Screens screens = new Screens();

        pdfService.generatePdf(screens);

        verify(pdfFiller).fill(pdfFields);
    }

    @Test
    void shouldReturnTheFilledPdf() {
        ApplicationFile expectedApplicationFile = new ApplicationFile("here is the pdf".getBytes(), "filename.pdf");
        when(pdfFiller.fill(any())).thenReturn(expectedApplicationFile);
        Screens screens = new Screens();

        ApplicationFile actualApplicationFile = pdfService.generatePdf(screens);

        assertThat(actualApplicationFile).isEqualTo(expectedApplicationFile);
    }
}