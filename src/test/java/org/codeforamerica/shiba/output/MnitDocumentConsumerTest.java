package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Dakota;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.UPPER_SIOUX;
import static org.codeforamerica.shiba.application.FlowType.FULL;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Document.XML;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.testutilities.TestUtils.getAbsoluteFilepath;
import static org.codeforamerica.shiba.testutilities.TestUtils.resetApplicationData;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import de.redsix.pdfcompare.PdfComparator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.FilenetWebServiceClient;
import org.codeforamerica.shiba.mnit.RoutingDestination;
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

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = NONE)
@ContextConfiguration(classes = {NonSessionScopedApplicationData.class})
@Tag("db")
class MnitDocumentConsumerTest {

  public static final byte[] FILE_BYTES = new byte[10];

  @MockBean
  private FeatureFlagConfiguration featureFlagConfig;
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private CountyMap<CountyRoutingDestination> countyMap;
  @Autowired
  private Map<String, TribalNationRoutingDestination> tribalNations;

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
  private DocumentStatusRepository documentStatusRepository;
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
    when(featureFlagConfig.get("submit-docs-via-email-for-hennepin")).thenReturn(FeatureFlag.ON);
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
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, routingDestination, application.getId(), CAF, FULL);
    verify(mnitClient).send(xmlApplicationFile, routingDestination, application.getId(), XML, FULL);
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
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, countyMap.get(expectedCounty), application.getId(),
        CAF, FULL);
    verify(mnitClient).send(xmlApplicationFile, countyMap.get(expectedCounty), application.getId(),
        XML, FULL);
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
        MILLE_LACS_BAND_OF_OJIBWE);
    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, routingDestination,
        application.getId(), CAF, FULL);
    verify(mnitClient).send(xmlApplicationFile, routingDestination,
        application.getId(), XML, FULL);
  }

  @Test
  void sendsToCountyIfTribalNationRoutingIsNotImplemented() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA"))
        .withPageData("selectTheTribe", "selectedTribe", List.of(UPPER_SIOUX))
        .withPageData("identifyCounty", "county", "Olmsted")
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build());

    documentConsumer.processCafAndCcap(application);

    CountyRoutingDestination olmstedRoutingDestination = countyMap.get(Olmsted);
    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, olmstedRoutingDestination);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, olmstedRoutingDestination, application.getId(), CAF,
        FULL);
    verify(mnitClient).send(xmlApplicationFile, olmstedRoutingDestination, application.getId(), XML,
        FULL);
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
        MILLE_LACS_BAND_OF_OJIBWE);
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
    verify(mnitClient, times(5)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(nationCaf, nationDestination, application.getId(), CAF, FULL);
    verify(mnitClient).send(xmlFile, nationDestination, application.getId(), XML, FULL);
    verify(mnitClient).send(countycaf, countyDestination, application.getId(), CAF, FULL);
    verify(mnitClient).send(xmlFile, countyDestination, application.getId(), XML, FULL);
    // CCAP never goes to Mille Lacs
    verify(mnitClient).send(ccap, countyDestination, application.getId(), CCAP, FULL);
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
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, countyMap.get(Olmsted), application.getId(), CCAP,
        FULL);
    verify(mnitClient).send(xmlApplicationFile, countyMap.get(Olmsted), application.getId(), XML,
        FULL);
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
    verify(documentStatusRepository).createOrUpdate(application.getId(),
        CAF, ((RoutingDestination) routingDestination).getName(), SENDING);
    verify(documentStatusRepository).createOrUpdate(application.getId(),
        CCAP, ((RoutingDestination) routingDestination).getName(), SENDING);
  }

  @Test
  void updatesStatusToDeliveryFailedForApplications() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), eq(CCAP), any(), any());

    doThrow(new RuntimeException()).when(mnitClient)
        .send(any(), any(), any(), eq(CCAP), any());
    doNothing().when(mnitClient).send(any(), any(), any(), eq(CAF), any());

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP", "SNAP"))
        .build());

    documentConsumer.processCafAndCcap(application);

    CountyRoutingDestination routingDestination = countyMap.get(Hennepin);
    verify(documentStatusRepository, times(1)).createOrUpdate(application.getId(),
        CCAP, ((RoutingDestination) routingDestination).getName(), SENDING);
    verify(documentStatusRepository, times(1)).createOrUpdate(application.getId(),
        CAF, ((RoutingDestination) routingDestination).getName(), SENDING);
    verify(documentStatusRepository, timeout(2000).atLeastOnce()).createOrUpdate(
        application.getId(),
        CCAP, ((RoutingDestination) routingDestination).getName(), DELIVERY_FAILED);
  }

  @Test
  void updatesStatusToDeliveryFailedForUploadedDocuments() throws IOException {
    mockDocUpload("shiba+file.jpg", "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");
    ApplicationFile testFile = new ApplicationFile(FILE_BYTES, "doc1of1.pdf");
    doReturn(testFile).when(pdfGenerator)
        .generate(anyString(), eq(UPLOADED_DOC), eq(CASEWORKER), any());
    doReturn(testFile).when(pdfGenerator)
        .generateForUploadedDocument(any(), anyInt(), eq(application), any());
    doThrow(new RuntimeException("Some mocked exception")).when(mnitClient)
        .send(any(), any(), any(), eq(UPLOADED_DOC), any());

    try {
      documentConsumer.processUploadedDocuments(application);
    } catch (Exception e) {
      // Catch mocked exception
    }

    CountyRoutingDestination routingDestination = countyMap.get(Olmsted);
    verify(documentStatusRepository, times(1)).createOrUpdate(application.getId(),
        UPLOADED_DOC, ((RoutingDestination) routingDestination).getName(), SENDING);
    verify(documentStatusRepository, timeout(2000).atLeastOnce()).createOrUpdate(
        application.getId(),
        UPLOADED_DOC, ((RoutingDestination) routingDestination).getName(), DELIVERY_FAILED);
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
    when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf"))
        .thenReturn("pdf1of2.pdf");
    when(fileNameGenerator.generateUploadedDocumentName(application, 1, "pdf"))
        .thenReturn("pdf2of2.pdf");

    documentConsumer.processUploadedDocuments(application);

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(mnitClient, times(2))
        .send(captor.capture(), eq(countyMap.get(Olmsted)), eq(application.getId()),
            eq(UPLOADED_DOC), any());
    verify(mnitClient, times(0)).send(any(), any(),
        eq(application.getId()), eq(XML), any()); // XMLs only for Dakota

    // Uncomment the following line to regenerate the test files (useful if the files or cover page have changed)
//         writeByteArrayToFile(captor.getAllValues().get(0).getFileBytes(), "src/test/resources/shiba+file.pdf");
//         writeByteArrayToFile(captor.getAllValues().get(1).getFileBytes(), "src/test/resources/test-uploaded-pdf-with-coverpage.pdf");
    // Assert that converted file contents are as expected
    verifyGeneratedPdf(captor.getAllValues().get(0).getFileBytes(), "shiba+file.pdf");
    verifyGeneratedPdf(captor.getAllValues().get(1).getFileBytes(),
        "test-uploaded-pdf-with-coverpage.pdf");
  }

  @Test
  void sendsXMLAndDocumentUploadsToDakota() throws IOException {
    new TestApplicationDataBuilder(applicationData)
        .withPageData("identifyCounty", "county", "Dakota");

    application.setFlow(LATER_DOCS);
    mockDocUpload("test-uploaded-pdf.pdf", "pdfS3FilePath", MediaType.APPLICATION_PDF_VALUE, "pdf");
    when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf"))
        .thenReturn("pdf1of1.pdf");
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    documentConsumer.processUploadedDocuments(application);

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(mnitClient).send(captor.capture(), eq(countyMap.get(Dakota)), eq(application.getId()),
        eq(UPLOADED_DOC), any());

    verify(mnitClient).send(eq(xmlApplicationFile), eq(countyMap.get(Dakota)),
        eq(application.getId()), eq(XML), any());
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

    verify(mnitClient, never()).send(any(), any(), any(), any(), any());
  }

  @Test
  void uploadedDocumentsAreSentToMilleLacsViaApiAndHennepinViaEmail() throws IOException {
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
    doReturn(testFile).when(pdfGenerator)
        .generate(anyString(), eq(UPLOADED_DOC), eq(CASEWORKER), any());
    doReturn(testFile).when(pdfGenerator)
        .generateForUploadedDocument(any(), anyInt(), eq(application), any());
    when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf")).thenReturn(
        "pdf1of1.pdf");
    when(fileNameGenerator.generateUploadedDocumentName(
        eq(application), eq(0), eq("pdf"), eq(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE)))
    ).thenReturn("MILLE_LACS_pdf1of1.pdf");

    documentConsumer.processUploadedDocuments(application);

    // Assert that only email is sent for Hennepin and api for Mille Lacs
    verify(mnitClient, times(1)).send(any(), any(), any(), any(), any());
    verify(mnitClient, never()).send(any(), eq(countyMap.get(Hennepin)),
        eq(application.getId()), eq(UPLOADED_DOC), eq(FULL));
    verify(emailClient, times(1)).sendHennepinDocUploadsEmails(eq(application), any());

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(mnitClient).send(captor.capture(), eq(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE)),
        eq(application.getId()), eq(UPLOADED_DOC), eq(FULL));
    assertThat(captor.getValue().getFileName()).isEqualTo("MILLE_LACS_pdf1of1.pdf");
  }

  @Test
  void uploadedDocumentsAreSentToHennepinViaApiWhenFlagIsOff() throws IOException {
    application.setCounty(Hennepin);
    new TestApplicationDataBuilder(application.getApplicationData())
        .withPageData("identifyCounty", "county", Hennepin.name())
        .withPageData("homeAddress", "enrichedCounty", Hennepin.name())
        .withPageData("homeAddress", "county", Hennepin.name());

    mockDocUpload("shiba+file.jpg", "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");
    when(featureFlagConfig.get("submit-docs-via-email-for-hennepin")).thenReturn(FeatureFlag.OFF);
    when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf")).thenReturn(
        "pdf1of1.pdf");

    documentConsumer.processUploadedDocuments(application);

    // Assert that only api is sent for Hennepin
    verify(mnitClient, times(1)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(any(), eq(countyMap.get(Hennepin)), eq(application.getId()),
        eq(UPLOADED_DOC), eq(FULL));
    verify(emailClient, never()).sendHennepinDocUploadsEmails(eq(application), any());
  }

  @Test
  void setsUploadedDocumentStatusToSendingWhenProcessUploadedDocumentsIsCalled()
      throws IOException {
    mockDocUpload("shiba+file.jpg", "someS3FilePath", MediaType.IMAGE_JPEG_VALUE, "jpg");

    mockDocUpload("test-uploaded-pdf.pdf", "pdfS3FilePath", MediaType.APPLICATION_PDF_VALUE, "pdf");

    when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf"))
        .thenReturn("pdf1of2.pdf");
    when(fileNameGenerator.generateUploadedDocumentName(application, 1, "pdf"))
        .thenReturn("pdf2of2.pdf");

    documentConsumer.processUploadedDocuments(application);

    CountyRoutingDestination routingDestination = countyMap.get(Olmsted);
    verify(documentStatusRepository, times(2)).createOrUpdate(application.getId(),
        UPLOADED_DOC, ((RoutingDestination) routingDestination).getName(), SENDING);
  }

  private void mockDocUpload(String uploadedDocFilename, String s3filepath, String contentType,
      String extension) throws IOException {
    var fileBytes = Files.readAllBytes(getAbsoluteFilepath(uploadedDocFilename));
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
