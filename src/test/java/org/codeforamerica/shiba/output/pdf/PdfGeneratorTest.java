package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfGeneratorTest {
    @Test
    void producesPdfFieldsAndFillsThePdf() {
        PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
        PdfFieldFiller pdfFieldFiller = mock(PdfFieldFiller.class);
        PdfGenerator pdfGenerator = new PdfGenerator(pdfFieldMapper, pdfFieldFiller);
        List<ApplicationInput> applicationInputs = List.of(new ApplicationInput("someGroupName", "someName", List.of("someValue"), ApplicationInputType.SINGLE_VALUE));

        List<PdfField> pdfFields = List.of(new SimplePdfField("someName", "someValue"));
        when(pdfFieldMapper.map(applicationInputs)).thenReturn(pdfFields);
        String applicationId = "someAppId";
        ApplicationFile expectedApplicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
        when(pdfFieldFiller.fill(pdfFields, applicationId))
                .thenReturn(expectedApplicationFile);

        ApplicationFile actualApplicationFile = pdfGenerator.generate(applicationInputs, applicationId);

        assertThat(actualApplicationFile).isEqualTo(expectedApplicationFile);
    }
}