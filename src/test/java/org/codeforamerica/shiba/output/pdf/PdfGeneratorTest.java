package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.applicationinputsmappers.FileNameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.Mockito.*;

class PdfGeneratorTest {
    PdfGenerator pdfGenerator;
    PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
    PdfFieldFiller caseworkerFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller clientFiller = mock(PdfFieldFiller.class);
    Map<Recipient, PdfFieldFiller> pdfFieldFillers = Map.of(
            CASEWORKER, caseworkerFiller,
            CLIENT, clientFiller
    );
    ApplicationInputsMappers mappers = mock(ApplicationInputsMappers.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    FileNameGenerator fileNameGenerator = mock(FileNameGenerator.class);

    @BeforeEach
    void setUp() {
        pdfGenerator = new PdfGenerator(
                pdfFieldMapper,
                pdfFieldFillers,
                applicationRepository,
                mappers,
                fileNameGenerator);
    }

    @Test
    void producesPdfFieldsAndFillsThePdf() {
        List<ApplicationInput> applicationInputs = List.of(new ApplicationInput("someGroupName", "someName", List.of("someValue"), ApplicationInputType.SINGLE_VALUE));

        String applicationId = "someAppId";
        List<PdfField> pdfFields = List.of(new SimplePdfField("someName", "someValue"));
        Application application = Application.builder()
                .id("")
                .completedAt(null)
                .applicationData(null)
                .county(null)
                .timeToComplete(null)
                .build();
        String fileName = "some file name";
        when(fileNameGenerator.generateFileName(application)).thenReturn(fileName);
        when(applicationRepository.find(applicationId)).thenReturn(application);
        Recipient recipient = CASEWORKER;
        when(mappers.map(application, recipient)).thenReturn(applicationInputs);
        when(pdfFieldMapper.map(applicationInputs)).thenReturn(pdfFields);
        ApplicationFile expectedApplicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
        when(caseworkerFiller.fill(pdfFields, applicationId, fileName))
                .thenReturn(expectedApplicationFile);

        ApplicationFile actualApplicationFile = pdfGenerator.generate(applicationId, recipient);

        assertThat(actualApplicationFile).isEqualTo(expectedApplicationFile);
    }

    @ParameterizedTest
    @EnumSource(Recipient.class)
    void shouldUseFillerRespectToRecipient(Recipient recipient) {
        pdfGenerator.generate("", recipient);

        verify(pdfFieldFillers.get(recipient)).fill(any(), any(), any());
    }
}