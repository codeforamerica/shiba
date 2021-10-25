package org.codeforamerica.shiba.output.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.*;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PdfGeneratorTest {

  private final String applicationId = "someApplicationId";
  private PdfGenerator pdfGenerator;
  private Application application;
  private PdfFieldMapper pdfFieldMapper;
  private PdfFieldFiller caseworkerFiller;
  private ApplicationInputsMappers mappers;
  private FileNameGenerator fileNameGenerator;
  private Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers;

  @BeforeEach
  void setUp() {
    pdfFieldMapper = mock(PdfFieldMapper.class);
    caseworkerFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller clientFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller ccapFiller = mock(PdfFieldFiller.class);
    mappers = mock(ApplicationInputsMappers.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    fileNameGenerator = mock(FileNameGenerator.class);

    pdfFieldFillers = Map.of(
        CASEWORKER, Map.of(Document.CAF, caseworkerFiller, Document.CCAP, ccapFiller),
        CLIENT, Map.of(Document.CAF, clientFiller, Document.CCAP, ccapFiller)
    );

    application = Application.builder()
        .id(applicationId)
        .completedAt(null)
        .applicationData(new ApplicationData())
        .county(null)
        .timeToComplete(null)
        .build();
    pdfGenerator = new PdfGenerator(
        pdfFieldMapper,
        pdfFieldFillers,
        applicationRepository,
        null,
        mappers,
        fileNameGenerator);
    when(applicationRepository.find(applicationId)).thenReturn(application);
  }

  @Test
  void producesPdfFieldsAndFillsThePdf() {
    List<ApplicationInput> applicationInputs = List
        .of(new ApplicationInput("someGroupName", "someName", List.of("someValue"),
            ApplicationInputType.SINGLE_VALUE));
    List<PdfField> pdfFields = List.of(new SimplePdfField("someName", "someValue"));
    String fileName = "some file name";
    when(fileNameGenerator.generatePdfFileName(application, Document.CAF)).thenReturn(fileName);
    Recipient recipient = CASEWORKER;
    when(mappers.map(application, Document.CAF, recipient)).thenReturn(applicationInputs);
    when(pdfFieldMapper.map(applicationInputs)).thenReturn(pdfFields);
    ApplicationFile expectedApplicationFile = new ApplicationFile("someContent".getBytes(),
        "someFileName");
    when(caseworkerFiller.fill(pdfFields, applicationId, fileName))
        .thenReturn(expectedApplicationFile);

    ApplicationFile actualApplicationFile = pdfGenerator
        .generate(applicationId, Document.CAF, recipient);

    assertThat(actualApplicationFile).isEqualTo(expectedApplicationFile);
  }

  @ParameterizedTest
  @EnumSource(Recipient.class)
  void shouldUseFillerRespectToRecipient(Recipient recipient) {
    pdfGenerator.generate(applicationId, Document.CAF, recipient);
    verify(pdfFieldFillers.get(recipient).get(Document.CAF)).fill(any(), any(), any());
  }
}
