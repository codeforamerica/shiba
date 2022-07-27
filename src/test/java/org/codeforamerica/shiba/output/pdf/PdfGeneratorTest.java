package org.codeforamerica.shiba.output.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.TribalNation.UpperSioux;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparers;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

class PdfGeneratorTest {

  private final String applicationId = "someApplicationId";
  private PdfGenerator pdfGenerator;
  private Application application;
  private PdfFieldMapper pdfFieldMapper;
  private PdfFieldFiller caseworkerFiller;
  private DocumentFieldPreparers preparers;
  private FilenameGenerator fileNameGenerator;
  private Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers;

  @Autowired
  ResourceLoader resourceLoader;
  
  @BeforeEach
  void setUp() {
    pdfFieldMapper = mock(PdfFieldMapper.class);
    caseworkerFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller caseworkerCafWdHouseholdSuppFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller clientCafWdHouseholdSuppFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller clientFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller ccapFiller = mock(PdfFieldFiller.class);
    resourceLoader = mock(ResourceLoader.class);
    
    preparers = mock(DocumentFieldPreparers.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    fileNameGenerator = mock(FilenameGenerator.class);
    FeatureFlagConfiguration featureFlags = mock(FeatureFlagConfiguration.class);
    ServicingAgencyMap<CountyRoutingDestination> countyMap = new ServicingAgencyMap<>();
    countyMap.setDefaultValue(
        new CountyRoutingDestination(Anoka, "dPId", "email", "555-5555")
    );

    pdfFieldFillers = Map.of(
        CASEWORKER, Map.of(Document.CAF, caseworkerFiller, Document.CCAP, ccapFiller),
        CLIENT, Map.of(Document.CAF, clientFiller, Document.CCAP, ccapFiller)
    );

    Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillers = Map.of(
        CASEWORKER, Map.of(Document.CAF, caseworkerCafWdHouseholdSuppFiller),
        CLIENT, Map.of(Document.CAF, clientCafWdHouseholdSuppFiller)
    );
    resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
    Resource coverPages = resourceLoader.getResource("cover-pages.pdf");
    Resource certainPops = resourceLoader.getResource("certain-pops.pdf");
    List<Resource> pdfResource = new ArrayList<Resource>();
    pdfResource.add(coverPages);
    pdfResource.add(certainPops);
    Map<Recipient, Map<String, List<Resource>>> pdfResourcesCertainPops = Map.of(
        CASEWORKER, Map.of("default", pdfResource),
        CLIENT, Map.of("default", pdfResource)
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
        pdfResourcesCertainPops,
        applicationRepository,
        null,
        preparers,
        fileNameGenerator,
        featureFlags,
        countyMap);
    when(applicationRepository.find(applicationId)).thenReturn(application);
  }

  @Test
  void generatesAPdfWithTheCorrectFilename() {
    TribalNationRoutingDestination routingDestination = new TribalNationRoutingDestination(
        UpperSioux, "dhsProviderId", "email", "phoneNumber");
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

    assertThat(actualApplicationFile.getFileName()).isEqualTo(expectedApplicationFile.getFileName());
  }
  
  @Test
  void shouldUseFillerForCertainPops() {
    List<DocumentField> documentFields = List
        .of(new DocumentField("someGroupName", "someName", List.of("someValue"),
            DocumentFieldType.SINGLE_VALUE));
    List<PdfField> pdfFields = List.of(new SimplePdfField("someName", "someValue"));
    String fileName = "someFileName";
    when(fileNameGenerator.generatePdfFilename(application, Document.CERTAIN_POPS)).thenReturn(fileName);
    Recipient recipient = CASEWORKER;
    when(preparers.prepareDocumentFields(application, Document.CERTAIN_POPS, recipient)).thenReturn(
        documentFields);
    when(pdfFieldMapper.map(documentFields)).thenReturn(pdfFields);
    ApplicationFile expectedApplicationFile = new ApplicationFile("someContent".getBytes(),
        "someFileName");
    

    ApplicationFile actualApplicationFile = pdfGenerator
        .generate(applicationId, Document.CERTAIN_POPS, recipient);

    assertThat(actualApplicationFile.getFileName()).isEqualTo(expectedApplicationFile.getFileName());
   
  }

  @ParameterizedTest
  @EnumSource(Recipient.class)
  void shouldUseFillerRespectToRecipient(Recipient recipient) {
    pdfGenerator.generate(applicationId, Document.CAF, recipient);
    verify(pdfFieldFillers.get(recipient).get(Document.CAF)).fill(any(), any(), any());
  }
  
 
}
