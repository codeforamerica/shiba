package org.codeforamerica.shiba.output.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.*;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparers;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
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
  private PdfFieldFiller caseworkerCafWdHouseholdSuppFiller;
  private DocumentFieldPreparers preparers;
  private FilenameGenerator fileNameGenerator;
  private Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers;
  private Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillers;
  private Map<Recipient, Map<Document, PdfFieldFiller>> ramseyPdfFieldFillers;
  private Map<Recipient, Map<Document, PdfFieldFiller>> ramseyPdfFieldWithCAFHHSuppFillers;
  private FeatureFlagConfiguration featureFlags;
  private  CountyMap<CountyRoutingDestination> countyMap;
  private CountyRoutingDestination defaultCountyRoutingDestination;
  @BeforeEach
  void setUp() {
    pdfFieldMapper = mock(PdfFieldMapper.class);
    caseworkerFiller = mock(PdfFieldFiller.class);
    caseworkerCafWdHouseholdSuppFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller clientCafWdHouseholdSuppFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller clientFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller ccapFiller = mock(PdfFieldFiller.class);

    PdfFieldFiller ramseyClientCafWdHouseholdSuppFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller ramseyClientFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller ramseyCcapFiller = mock(PdfFieldFiller.class);
    preparers = mock(DocumentFieldPreparers.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    fileNameGenerator = mock(FilenameGenerator.class);
    FileToPDFConverter pdfWordConverter = mock(FileToPDFConverter.class);
    featureFlags = mock(FeatureFlagConfiguration.class);
    countyMap = new CountyMap<>();
    defaultCountyRoutingDestination = CountyRoutingDestination.builder()
                    .county(County.Anoka).phoneNumber("555-5555").build();
    countyMap.setDefaultValue(defaultCountyRoutingDestination);


    pdfFieldFillers = Map.of(
        CASEWORKER, Map.of(Document.CAF, caseworkerFiller, Document.CCAP, ccapFiller),
        CLIENT, Map.of(Document.CAF, clientFiller, Document.CCAP, ccapFiller)
    );
    
    pdfFieldWithCAFHHSuppFillers = Map.of(
        CASEWORKER, Map.of(Document.CAF, caseworkerCafWdHouseholdSuppFiller),
        CLIENT, Map.of(Document.CAF, clientCafWdHouseholdSuppFiller)
    );

    ramseyPdfFieldFillers = Map.of(
            CASEWORKER, Map.of(Document.CAF, caseworkerFiller, Document.CCAP, ramseyCcapFiller),
            CLIENT, Map.of(Document.CAF, ramseyClientFiller, Document.CCAP, ramseyCcapFiller)
    );

    ramseyPdfFieldWithCAFHHSuppFillers = Map.of(
            CASEWORKER, Map.of(Document.CAF, caseworkerCafWdHouseholdSuppFiller),
            CLIENT, Map.of(Document.CAF, ramseyClientCafWdHouseholdSuppFiller)
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
        pdfFieldWithCAFHHSuppFillers,
        ramseyPdfFieldFillers,
        ramseyPdfFieldWithCAFHHSuppFillers,
        applicationRepository,
        null,
        preparers,
        fileNameGenerator,
        pdfWordConverter,
        featureFlags,
        countyMap);
    when(applicationRepository.find(applicationId)).thenReturn(application);
  }

  @Test
  void generatesAPdfWithTheCorrectFilename() {
    TribalNationRoutingDestination routingDestination = new TribalNationRoutingDestination(
        "nationName", "dhsProviderId", "email", "phoneNumber");
    doReturn("destinationSpecificDestination").when(fileNameGenerator)
        .generatePdfFilename(any(), any(), any());

    pdfGenerator.generate(applicationId, Document.CAF, CASEWORKER, routingDestination);
    verify(fileNameGenerator).generatePdfFilename(application, Document.CAF,
        routingDestination);
    verify(pdfFieldFillers.get(CASEWORKER).get(Document.CAF)).fill(any(), eq(applicationId),
        eq("destinationSpecificDestination"));
  }

  @Test
  void producesPdfFieldsAndFillsThePdf() {
    List<DocumentField> documentFields = List
        .of(new DocumentField("someGroupName", "someName", List.of("someValue"),
            DocumentFieldType.SINGLE_VALUE));
    List<PdfField> pdfFields = List.of(new SimplePdfField("someName", "someValue"));
    String fileName = "some file name";
    when(fileNameGenerator.generatePdfFilename(application, Document.CAF)).thenReturn(fileName);
    Recipient recipient = CASEWORKER;
    when(preparers.prepareDocumentFields(application, Document.CAF, recipient)).thenReturn(
        documentFields);
    when(pdfFieldMapper.map(documentFields)).thenReturn(pdfFields);
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
