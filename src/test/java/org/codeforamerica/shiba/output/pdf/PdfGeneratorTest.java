package org.codeforamerica.shiba.output.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.TribalNation.UpperSioux;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.codeforamerica.shiba.testutilities.TestUtils.getFileContentsAsByteArray;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.UploadedDocsPreparer;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparers;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.web.servlet.MockMvc;

class PdfGeneratorTest {

  private final String applicationId = "someApplicationId";
  private PdfGenerator pdfGenerator;
  private Application application;
  private PdfFieldMapper pdfFieldMapper;
  private PdfFieldFiller caseworkerFiller;
  private DocumentFieldPreparers preparers;
  private FilenameGenerator fileNameGenerator;
  private Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers;
  MockMvc mockMvc;
  XmlGenerator xmlGenerator = mock(XmlGenerator.class);
  ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
  UploadedDocsPreparer uploadedDocsPreparer = mock(UploadedDocsPreparer.class);
  DocumentRepository documentRepository;

  @Autowired
  ResourceLoader resourceLoader;
  
  @BeforeEach
  void setUp() {
    pdfFieldMapper = mock(PdfFieldMapper.class);
    caseworkerFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller caseworkerCafWdHouseholdSuppFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller clientCafWdHouseholdSuppFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller caseworkerCafWdHouseholdSuppFiller2 = mock(PdfFieldFiller.class);
    PdfFieldFiller clientCafWdHouseholdSuppFiller2 = mock(PdfFieldFiller.class);
    PdfFieldFiller clientFiller = mock(PdfFieldFiller.class);
    PdfFieldFiller ccapFiller = mock(PdfFieldFiller.class);
    resourceLoader = mock(ResourceLoader.class);
    documentRepository = mock(DocumentRepository.class);
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
    
    Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillers2 = Map.of(
        CASEWORKER, Map.of(Document.CAF, caseworkerCafWdHouseholdSuppFiller2),
        CLIENT, Map.of(Document.CAF, clientCafWdHouseholdSuppFiller2)
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
        pdfFieldWithCAFHHSuppFillers2,
        pdfResourcesCertainPops,
        applicationRepository,
        documentRepository,
        preparers,
        fileNameGenerator,
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
  
  @Test
  void shouldAddMNbenefitsSubmissionDate() throws Exception {
    var image = getFileContentsAsByteArray("shiba+file.jpg");
    var coverPage = getFileContentsAsByteArray("test-cover-pages.pdf");
    var applicationId = "9870000123";
   
    ApplicationFile coverPageFile = new ApplicationFile(coverPage, "");
    UploadedDocument uploadedDoc = new UploadedDocument("shiba+file.jpg", "", "", "", image.length);

    ApplicationData applicationData = new ApplicationData();
    applicationData.setId(applicationId);
    applicationData.setUploadedDocs(List.of(uploadedDoc));
    applicationData.setFlow(FlowType.LATER_DOCS);
    Application application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .completedAt(ZonedDateTime.parse("2023-03-22T13:48:39.213+00:00[America/Chicago]"))
        .build();
    List<UploadedDocument> uploadedDocumentList = List.of(uploadedDoc);
    when(documentRepository.get(any())).thenReturn(image);
    List<ApplicationFile> applicationFileList = pdfGenerator.generateCombinedUploadedDocument(uploadedDocumentList, application, coverPageFile.getFileBytes());
    String text = null;
    for(ApplicationFile af:applicationFileList) {
     PDDocument doc = Loader.loadPDF(af.getFileBytes());
     PDFTextStripper findPhrase = new PDFTextStripper();
     text = findPhrase.getText(doc);
    }
    assertThat(text).contains("MNbenefits: 03/22/2023 08:48:39 AM");
  }
  
  @Test
  void shouldAddMNbenefitsSubmissionDateToEncryptedPdf() throws Exception {
    var image = getFileContentsAsByteArray("shiba+file_encrypted_pdf.pdf");
    var coverPage = getFileContentsAsByteArray("test-cover-pages.pdf");
    var applicationId = "9870000123";
   
    ApplicationFile coverPageFile = new ApplicationFile(coverPage, "");
    UploadedDocument uploadedDoc = new UploadedDocument("shiba+file_encrypted_pdf.pdf", "", "", "", image.length);

    ApplicationData applicationData = new ApplicationData();
    applicationData.setId(applicationId);
    applicationData.setUploadedDocs(List.of(uploadedDoc));
    applicationData.setFlow(FlowType.LATER_DOCS);
    Application application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .completedAt(ZonedDateTime.parse("2023-03-22T13:48:39.213+00:00[America/Chicago]"))
        .build();
    List<UploadedDocument> uploadedDocumentList = List.of(uploadedDoc);
    when(documentRepository.get(any())).thenReturn(image);
    List<ApplicationFile> applicationFileList = pdfGenerator.generateCombinedUploadedDocument(uploadedDocumentList, application, coverPageFile.getFileBytes());
    String text = null;
    for(ApplicationFile af:applicationFileList) {
     PDDocument doc = Loader.loadPDF(af.getFileBytes());
     PDFTextStripper findPhrase = new PDFTextStripper();
     text = findPhrase.getText(doc);
    }
    assertThat(text).contains("MNbenefits: 03/22/2023 08:48:39 AM");
  }
  
 
}
