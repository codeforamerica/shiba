package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PdfGeneratorTest {
    @Test
    void producesPdfFieldsAndFillsThePdf() {
        PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
        PdfFieldFiller pdfFieldFiller = mock(PdfFieldFiller.class);
        ApplicationInputsMappers mappers = mock(ApplicationInputsMappers.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

        PdfGenerator pdfGenerator = new PdfGenerator(
                pdfFieldMapper,
                pdfFieldFiller,
                applicationRepository,
                mappers);
        List<ApplicationInput> applicationInputs = List.of(new ApplicationInput("someGroupName", "someName", List.of("someValue"), ApplicationInputType.SINGLE_VALUE));

        String applicationId = "someAppId";
        List<PdfField> pdfFields = List.of(new SimplePdfField("someName", "someValue"));
        Application application = Application.builder()
                .id("")
                .completedAt(null)
                .applicationData(null)
                .county(null)
                .fileName("some file name")
                .timeToComplete(null)
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);
        Recipient recipient = CASEWORKER;
        when(mappers.map(application, recipient)).thenReturn(applicationInputs);
        when(pdfFieldMapper.map(applicationInputs)).thenReturn(pdfFields);
        ApplicationFile expectedApplicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
        when(pdfFieldFiller.fill(pdfFields, applicationId, application.getFileName()))
                .thenReturn(expectedApplicationFile);

        ApplicationFile actualApplicationFile = pdfGenerator.generate(applicationId, recipient);

        assertThat(actualApplicationFile).isEqualTo(expectedApplicationFile);
    }
}