package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Dakota;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.TribalNation.MilleLacsBandOfOjibwe;
import static org.codeforamerica.shiba.TribalNation.UpperSioux;
import static org.codeforamerica.shiba.application.FlowType.FULL;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.application.Status.UNDELIVERABLE;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Document.XML;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.testutilities.TestUtils.getAbsoluteFilepath;
import static org.codeforamerica.shiba.testutilities.TestUtils.resetApplicationData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.FilenetWebServiceClient;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.testutilities.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import de.redsix.pdfcompare.PdfComparator;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = {NonSessionScopedApplicationData.class})
@Tag("db")
class MnitDocumentConsumerTest {

  public static final byte[] FILE_BYTES = new byte[10];

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private ServicingAgencyMap<CountyRoutingDestination> countyMap;
  @Autowired
  private ServicingAgencyMap<TribalNationRoutingDestination> tribalNations;

  @MockBean
  private FeatureFlagConfiguration featureFlagConfig;
  @MockBean
  private FilenetWebServiceClient mnitClient;
  @MockBean
  private EmailClient emailClient;
  @MockBean
  private XmlGenerator xmlGenerator;
  @MockBean
  private MonitoringService monitoringService;
  @MockBean
  private DocumentRepository documentRepository;
  @MockBean
  private ClientRegistrationRepository repository;
  @MockBean
  private FilenameGenerator fileNameGenerator;
  @MockBean
  private ApplicationRepository applicationRepository;
  @MockBean
  private ApplicationStatusRepository applicationStatusRepository;
  @MockBean
  private MessageSource messageSource;
  @SpyBean
  private PdfGenerator pdfGenerator;

  @Autowired
  private ApplicationData applicationData;
  @Autowired
  private MnitDocumentConsumer documentConsumer;

  private Application application;

  @BeforeEach
  void setUp() {
    applicationData = new TestApplicationDataBuilder(applicationData)
        .withPersonalInfo()
        .withContactInfo()
        .withApplicantPrograms(List.of("SNAP"))
        .withPageData("identifyCounty", "county", "Olmsted")
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build();

    ZonedDateTime completedAt = ZonedDateTime.of(
        LocalDateTime.of(2021, 6, 10, 1, 28),
        ZoneOffset.UTC);

    application = Application.builder()
        .id("someId")
        .completedAt(completedAt)
        .applicationData(applicationData)
        .county(Olmsted)
        .timeToComplete(null)
        .flow(FULL)
        .build();
    when(messageSource.getMessage(any(), any(), any())).thenReturn("default success message");
    when(fileNameGenerator.generatePdfFilename(any(), any())).thenReturn("some-file.pdf");
    when(featureFlagConfig.get("filenet")).thenReturn(FeatureFlag.ON);

    doReturn(application).when(applicationRepository).find(any());
  }

  @AfterEach
  void afterEach() {
    resetApplicationData(applicationData);
  }

  @Test
  void sendsTheGeneratedXmlAndPdfToCountyOnly() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    documentConsumer.processCafAndCcap(application);

    CountyRoutingDestination routingDestination = countyMap.get(Olmsted);
    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any());
    verify(mnitClient).send(application, pdfApplicationFile, routingDestination, CAF);
    verify(mnitClient).send(application, xmlApplicationFile, routingDestination, XML);
  }

  @ParameterizedTest
  @CsvSource({
      "YellowMedicine,YellowMedicine",
      "Aitkin,Aitkin",
      "LakeOfTheWoods,LakeOfTheWoods",
      "StLouis,StLouis",
      "LacQuiParle,LacQuiParle"})
  void sendsTheGeneratedXmlAndPdfToNewCounty(String countyName, County expectedCounty) {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA"))
        .withPageData("homeAddress", "county", List.of(countyName))
        .withPageData("identifyCounty", "county", countyName)
        .build());
    application.setCounty(expectedCounty);

    documentConsumer.processCafAndCcap(application);

    CountyRoutingDestination routingDestination = countyMap.get(expectedCounty);
    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any());
    verify(mnitClient).send(application, pdfApplicationFile, countyMap.get(expectedCounty), CAF);
    verify(mnitClient).send(application, xmlApplicationFile, countyMap.get(expectedCounty), XML);
  }

  @Test
  void sendsToTribalNationOnly() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA"))
        .withPageData("selectTheTribe", "selectedTribe", List.of("Bois Forte"))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .build());

    documentConsumer.processCafAndCcap(application);

    TribalNationRoutingDestination routingDestination = tribalNations.get(
        MilleLacsBandOfOjibwe);
    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any());
    verify(mnitClient).send(application, pdfApplicationFile, routingDestination, CAF);
    verify(mnitClient).send(application, xmlApplicationFile, routingDestination, XML);
  }

  @Test
  void sendsToCountyIfTribalNationRoutingIsNotImplemented() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA"))
        .withPageData("selectTheTribe", "selectedTribe", List.of(UpperSioux.toString()))
        .withPageData("identifyCounty", "county", "Olmsted")
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build());

    documentConsumer.processCafAndCcap(application);

    CountyRoutingDestination olmstedRoutingDestination = countyMap.get(Olmsted);
    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, olmstedRoutingDestination);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any());
    verify(mnitClient).send(application, pdfApplicationFile, olmstedRoutingDestination, CAF);
    verify(mnitClient).send(application, xmlApplicationFile, olmstedRoutingDestination,  XML);
  }

  @Test
  void sendsToBothTribalNationAndCounty() {
    // set up county caf mock
    CountyRoutingDestination countyDestination = countyMap.get(Olmsted);
    ApplicationFile countycaf = new ApplicationFile("mycaf".getBytes(), "countycaf.pdf");
    doReturn(countycaf).when(pdfGenerator)
        .generate(anyString(), eq(CAF), any(), eq(countyDestination));

    // set up tribal nation caf mock
    TribalNationRoutingDestination nationDestination = tribalNations.get(
        MilleLacsBandOfOjibwe);
    ApplicationFile nationCaf = new ApplicationFile("mycaf".getBytes(), "tribalNationCaf.pdf");
    doReturn(nationCaf).when(pdfGenerator)
        .generate(anyString(), eq(CAF), any(), eq(nationDestination));

    // set up ccap mock
    ApplicationFile ccap = new ApplicationFile("myccap".getBytes(), "ccap.pdf");
    doReturn(ccap).when(pdfGenerator).generate(anyString(), eq(CCAP), any(), eq(countyDestination));

    ApplicationFile xmlFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA", "SNAP", "CCAP"))
        .withPageData("selectTheTribe", "selectedTribe", List.of("Bois Forte"))
        .withPageData("identifyCounty", "county", "Olmsted")
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build());

    documentConsumer.processCafAndCcap(application);

    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, countyDestination);
    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, nationDestination);
    verify(pdfGenerator).generate(application.getId(), CCAP, CASEWORKER, countyDestination);
    verify(xmlGenerator, times(2)).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(5)).send(any(), any(), any(), any());
    verify(mnitClient).send(application, nationCaf, nationDestination, CAF);
    verify(mnitClient).send(application, xmlFile, nationDestination, XML);
    verify(mnitClient).send(application, countycaf, countyDestination, CAF);
    verify(mnitClient).send(application, xmlFile, countyDestination, XML);
    // CCAP never goes to Mille Lacs
    verify(mnitClient).send(application, ccap, countyDestination, CCAP);
  }

  @Test
  void sendsTheCcapPdfIfTheApplicationHasCCAP() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), eq(CCAP), any(), any());

    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("identifyCounty", "county", "Olmsted")
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build());

    documentConsumer.processCafAndCcap(application);

    // Send CCAP and XML (but not CAF)
    verify(mnitClient, times(2)).send(any(), any(), any(), any());
    verify(mnitClient).send(application, pdfApplicationFile, countyMap.get(Olmsted), CCAP);
    verify(mnitClient).send(application, xmlApplicationFile, countyMap.get(Olmsted), XML);
  }

  @Test
  void updatesStatusToSendingForCafAndCcapDocuments() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), eq(CCAP), any(), any());

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP", "SNAP"))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .build());

    documentConsumer.processCafAndCcap(application);

    CountyRoutingDestination routingDestination = countyMap.get(Hennepin);
    verify(applicationStatusRepository).createOrUpdate(application.getId(),
        CAF, routingDestination.getName(), SENDING,null);
    verify(applicationStatusRepository).createOrUpdate(application.getId(),
        CCAP, routingDestination.getName(), SENDING,"someFile.pdf");
  }

  @Test
  void updatesStatusToDeliveryFailedForApplications() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), eq(CCAP), any(), any());

    doThrow(new RuntimeException()).when(mnitClient)
        .send(eq(application), any(), any(), eq(CCAP));
    doNothing().when(mnitClient).send(eq(application), any(), any(), eq(CAF));

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP", "SNAP"))
        .build());

    documentConsumer.processCafAndCcap(application);

    CountyRoutingDestination routingDestination = countyMap.get(Hennepin);
    verify(applicationStatusRepository, times(1)).createOrUpdate(application.getId(),
        CCAP, routingDestination.getName(), SENDING,"someFile.pdf");
    verify(applicationStatusRepository, times(1)).createOrUpdate(application.getId(),
        CAF, routingDestination.getName(), SENDING,null);
    verify(applicationStatusRepository, timeout(2000).atLeastOnce()).createOrUpdate(
        application.getId(),
        CCAP, routingDestination.getName(), DELIVERY_FAILED,"someFile.pdf");
  }

  @Test
  void updatesStatusToDeliveryFailedForUploadedDocuments() throws IOException {
    mockDocUpload("shiba+file.jpg", "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");
    ApplicationFile testFile = new ApplicationFile(FILE_BYTES, "doc1of1.pdf");
    List<ApplicationFile> testFileList = List.of(new ApplicationFile(FILE_BYTES, "doc1of1.pdf"));
    doReturn(testFile).when(pdfGenerator)
        .generate(anyString(), eq(UPLOADED_DOC), eq(CASEWORKER), any());
    doReturn(testFileList).when(pdfGenerator)
        .generateCombinedUploadedDocument(any(), eq(application), any(), any());
    doThrow(new RuntimeException("Some mocked exception")).when(mnitClient)
        .send(eq(application), any(), any(), eq(UPLOADED_DOC));

    try {
      documentConsumer.processUploadedDocuments(application);
    } catch (Exception e) {
      // Catch mocked exception
    }

    CountyRoutingDestination routingDestination = countyMap.get(Olmsted);
    verify(applicationStatusRepository, times(1)).createOrUpdate(application.getId(),
        UPLOADED_DOC, routingDestination.getName(), SENDING,null);
    verify(applicationStatusRepository, timeout(2000).atLeastOnce()).createOrUpdate(
        application.getId(),
        UPLOADED_DOC, routingDestination.getName(), DELIVERY_FAILED,null);
  }

  @Test
  void sendsApplicationIdToMonitoringService() {
    documentConsumer.processCafAndCcap(application);
    verify(monitoringService).setApplicationId(application.getId());
  }

  @Test
  void sendsBothImageAndDocumentUploadsSuccessfully() throws IOException {
    mockDocUpload("shiba+file.jpg", "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");

    mockDocUpload("test-uploaded-pdf.pdf", "pdfS3FilePath", MediaType.APPLICATION_PDF_VALUE, "pdf");

    when(fileNameGenerator.generateUploadedDocumentName(eq(application),anyInt(),eq("pdf"),any(),anyInt())).thenReturn("combined-pdf.pdf");
    documentConsumer.processUploadedDocuments(application);

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(mnitClient, times(1))
        .send(eq(application), captor.capture(), eq(countyMap.get(Olmsted)),
            eq(UPLOADED_DOC));
    verify(mnitClient, times(0)).send(eq(application), any(), any(),
        eq(XML)); // XMLs only for Dakota

    // Uncomment the following lines to regenerate the test files (useful if the files or cover page have changed)
//        Utils.writeByteArrayToFile(captor.getAllValues().get(0).getFileBytes(), "src/test/resources/shiba+file.pdf");
//        
    //Utils.writeByteArrayToFile(captor.getAllValues().get(0).getFileBytes(), "src/test/resources/combined-pdf.pdf");
   

    // Assert that converted file contents are as expected
    verifyGeneratedPdf(captor.getAllValues().get(0).getFileBytes(), "combined-pdf.pdf");
  }

  // There are two flows to consider in regards to sending XML for uploaded docs to Dakota County.
  @ParameterizedTest
  @CsvSource({
      "identifyCountyOrTribalNation,LATER_DOCS",
      "healthcareRenewalUpload,HEALTHCARE_RENEWAL"})
  void sendsXMLAndDocumentUploadsToDakota(String pageName, String flowName) throws IOException {
	// set the application-level attributes
	FlowType flowType = FlowType.valueOf(flowName);
	application.setFlow(flowType);
	County county = County.Dakota;
	application.setCounty(county);
	
	// set the applicationData-level attributes
	// first, remove the "identifyCounty" page that is automatically created in the setup.
	applicationData.getPagesData().remove("identifyCounty");
    new TestApplicationDataBuilder(applicationData)
        .withPageData(pageName, "county", "Dakota");
    applicationData.setFlow(flowType);	

    mockDocUpload("test-uploaded-pdf.pdf", "pdfS3FilePath", MediaType.APPLICATION_PDF_VALUE, "pdf");
    when(fileNameGenerator.generateUploadedDocumentName(eq(application), eq(0), eq("pdf"), any(), eq(1)))
        .thenReturn("pdf1of1.pdf");
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    documentConsumer.processUploadedDocuments(application);

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(mnitClient).send(eq(application), captor.capture(), eq(countyMap.get(Dakota)), eq(UPLOADED_DOC));

    verify(mnitClient).send(eq(application), eq(xmlApplicationFile), eq(countyMap.get(Dakota)), eq(XML));
  }

  @Test
  void uploadedDocumentDoesNotSendToMnitIfNull() {
    String uploadedDocFilename = "someName";
    String s3filepath = "originalName.jpg";
    String contentType = MediaType.IMAGE_JPEG_VALUE;
    String extension = "jpg";
    byte[] fileBytes = null;
    when(documentRepository.get("")).thenReturn(fileBytes);

    applicationData.addUploadedDoc(
        new MockMultipartFile(uploadedDocFilename, s3filepath + extension, contentType, fileBytes),
        "",
        "someDataUrl",
        MediaType.IMAGE_JPEG_VALUE);
    when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf")).thenReturn(
        "pdf1of2.pdf");

    documentConsumer.processUploadedDocuments(application);

    verify(mnitClient, never()).send(any(), any(), any(), any());
  }

  @Test
  void uploadedDocumentsAreSentToMilleLacsViaApiAndHennepin() throws IOException {
    // Set county to Hennepin and tribal nation to Bois Forte
    application.setCounty(Hennepin);
    new TestApplicationDataBuilder(application.getApplicationData())
        .withApplicantPrograms(List.of("EA", "SNAP", "CCAP"))
        .withPageData("identifyCounty", "county", Hennepin.name())
        .withPageData("selectTheTribe", "selectedTribe", "Bois Forte")
        .withPageData("homeAddress", "enrichedCounty", "Hennepin")
        .withPageData("homeAddress", "county", "Hennepin");

    mockDocUpload("shiba+file.jpg", "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");
    ApplicationFile testFile = new ApplicationFile(FILE_BYTES, "doc1of1.pdf");
    List<ApplicationFile> testFileList = List.of(new ApplicationFile(FILE_BYTES, "doc1of1.pdf"));
    doReturn(testFile).when(pdfGenerator)
        .generate(anyString(), eq(UPLOADED_DOC), eq(CASEWORKER), any());
    doReturn(testFileList).when(pdfGenerator)
        .generateCombinedUploadedDocument(any(), eq(application),any(), any());
    when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf")).thenReturn(
        "pdf1of1.pdf");
    when(fileNameGenerator.generateUploadedDocumentName(
        eq(application), eq(0), eq("pdf"), eq(tribalNations.get(MilleLacsBandOfOjibwe)), eq(1))
    ).thenReturn("MILLE_LACS_pdf1of1.pdf");
    when(fileNameGenerator.generateUploadedDocumentName(eq(application),anyInt(),eq("pdf"),eq(tribalNations.get(MilleLacsBandOfOjibwe)),anyInt()))
   // when(fileNameGenerator.generateCombinedUploadedDocsName(eq(application), eq("pdf"), eq(tribalNations.get(MilleLacsBandOfOjibwe))))
    .thenReturn("MILLE_LACS_pdf.pdf");

    documentConsumer.processUploadedDocuments(application);

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(mnitClient).send(eq(application), captor.capture(), eq(tribalNations.get(MilleLacsBandOfOjibwe)), eq(UPLOADED_DOC));
    assertThat(captor.getValue().getFileName()).isEqualTo("MILLE_LACS_pdf.pdf");
  }

  @Test
  void setsUploadedDocumentStatusToSendingWhenProcessUploadedDocumentsIsCalled()
      throws IOException {
    mockDocUpload("shiba+file.jpg", "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");

    mockDocUpload("test-uploaded-pdf.pdf", "pdfS3FilePath", MediaType.APPLICATION_PDF_VALUE, "pdf");

    when(fileNameGenerator.generateUploadedDocumentName(eq(application), eq(0), eq("pdf"), any(), eq(1)))
        .thenReturn("pdf1of1.pdf");
   /* when(fileNameGenerator.generateUploadedDocumentName(eq(application), eq(1), eq("pdf"), any(), eq(2)))
        .thenReturn("pdf2of2.pdf");*/

    documentConsumer.processUploadedDocuments(application);

    CountyRoutingDestination routingDestination = countyMap.get(Olmsted);
    verify(applicationStatusRepository,times(1)).createOrUpdate(application.getId(),
        UPLOADED_DOC, routingDestination.getName(), SENDING, "pdf1of1.pdf");
  }

  @Test
  void setsUploadedDocumentStatusToUndeliverableWhenUploadedDocumentsAreEmpty() {
    mockDocUpload(new byte[]{}, "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");

    documentConsumer.processUploadedDocuments(application);

    verify(applicationStatusRepository).createOrUpdateAllForDocumentType(
        application, UNDELIVERABLE, UPLOADED_DOC);
  }

  private void mockDocUpload(String uploadedDocFilename, String s3filepath, String contentType,
      String extension) throws IOException {
    var fileBytes = Files.readAllBytes(getAbsoluteFilepath(uploadedDocFilename));
    mockDocUpload(fileBytes, s3filepath, contentType, extension);
  }

  private void mockDocUpload(byte[] fileBytes, String s3filepath, String contentType,
      String extension) {
    when(documentRepository.get(s3filepath)).thenReturn(fileBytes);
    applicationData.addUploadedDoc(
        new MockMultipartFile("someName", "originalName." + extension, contentType, fileBytes),
        s3filepath,
        "someDataUrl",
        contentType);
  }

  private void verifyGeneratedPdf(byte[] actualFileBytes, String expectedFile) throws IOException {
    try (var actual = new ByteArrayInputStream(actualFileBytes);
        var expected = Files.newInputStream(getAbsoluteFilepath(expectedFile))) {
      var compareResult = new PdfComparator<>(expected, actual).compare();
//            compareResult.writeTo("diffOutput"); // uncomment this line to print the diff between the two pdfs
      assertThat(compareResult.isEqual()).isTrue();
    }
  }
}
