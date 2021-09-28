package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.TribalNation.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.TribalNation.UPPER_SIOUX;
import static org.codeforamerica.shiba.application.FlowType.FULL;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.testutilities.TestUtils.getAbsoluteFilepath;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

  @Autowired
  private CountyMap<CountyRoutingDestination> countyMap;
  @Autowired
  private Map<String, TribalNation> tribalNations;
  @MockBean
  private MnitEsbWebServiceClient mnitClient;
  @MockBean
  private XmlGenerator xmlGenerator;
  @MockBean
  private MonitoringService monitoringService;
  @MockBean
  private DocumentRepository documentRepository;
  @MockBean
  private ClientRegistrationRepository repository;
  @MockBean
  private FileNameGenerator fileNameGenerator;
  @MockBean
  private ApplicationRepository applicationRepository;
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
    applicationData = new TestApplicationDataBuilder()
        .withPersonalInfo()
        .withContactInfo()
        .withApplicantPrograms(List.of("SNAP"))
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
    when(fileNameGenerator.generatePdfFileName(any(), any())).thenReturn("some-file.pdf");
    doReturn(application).when(applicationRepository).find(any());
  }

  @AfterEach
  void afterEach() {
    applicationData.setUploadedDocs(new ArrayList<>());
  }

  @Test
  void sendsTheGeneratedXmlAndPdfToCountyOnly() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    documentConsumer.processCafAndCcap(application);

    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, countyMap.get(Olmsted), application.getId(), CAF,
        FULL);
    verify(mnitClient).send(xmlApplicationFile, countyMap.get(Olmsted), application.getId(), CAF,
        FULL);
  }

  @Test
  void sendsToTribalNationOnly() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA"))
        .withPageData("selectTheTribe", "selectedTribe", List.of("Bois Forte"))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .build());

    documentConsumer.processCafAndCcap(application);

    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE),
        application.getId(), CAF, FULL);
    verify(mnitClient).send(xmlApplicationFile, tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE),
        application.getId(), CAF, FULL);
  }

  @Test
  void sendsToCountyIfTribalNationRoutingIsNotImplemented() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA"))
        .withPageData("selectTheTribe", "selectedTribe", List.of(UPPER_SIOUX))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build());

    documentConsumer.processCafAndCcap(application);

    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(2)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, countyMap.get(Olmsted), application.getId(), CAF,
        FULL);
    verify(mnitClient).send(xmlApplicationFile, countyMap.get(Olmsted), application.getId(), CAF,
        FULL);
  }

  @Test
  void sendsToBothTribalNationAndCounty() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any());
    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("EA", "SNAP", "CCAP"))
        .withPageData("selectTheTribe", "selectedTribe", List.of("Bois Forte"))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build());

    documentConsumer.processCafAndCcap(application);

    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    verify(mnitClient, times(5)).send(any(), any(), any(), any(), any());
    verify(mnitClient).send(pdfApplicationFile, tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE),
        application.getId(), CAF, FULL);
    verify(mnitClient).send(xmlApplicationFile, tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE),
        application.getId(), CAF, FULL);
    verify(mnitClient).send(pdfApplicationFile, countyMap.get(Olmsted), application.getId(), CAF,
        FULL);
    verify(mnitClient).send(xmlApplicationFile, countyMap.get(Olmsted), application.getId(), CAF,
        FULL);
    // CCAP never goes to Mille Lacs
    verify(mnitClient).send(pdfApplicationFile, countyMap.get(Olmsted), application.getId(), CCAP,
        FULL);
  }

  @Test
  void sendsTheCcapPdfIfTheApplicationHasCCAP() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), eq(CCAP), any());

    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
    when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP"))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
        .build());

    documentConsumer.processCafAndCcap(application);

    verify(mnitClient).send(pdfApplicationFile, countyMap.get(Olmsted), application.getId(), CCAP,
        FULL);
  }

  @Test
  void updatesStatusToSendingForCafAndCcapDocuments() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), eq(CCAP), any());

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP", "SNAP"))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .build());

    documentConsumer.processCafAndCcap(application);

    verify(applicationRepository).updateStatus(application.getId(), CAF, SENDING);
    verify(applicationRepository).updateStatus(application.getId(), CCAP, SENDING);
  }

  @Test
  void updatesStatusToDeliveryFailedForDocuments() {
    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), eq(CCAP), any());

    doThrow(new RuntimeException()).doNothing().when(mnitClient)
        .send(any(), any(), any(), any(), any());

    application.setApplicationData(new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP", "SNAP"))
        .withPageData("homeAddress", "county", List.of("Olmsted"))
        .build());

    documentConsumer.processCafAndCcap(application);

    verify(applicationRepository, atLeastOnce())
        .updateStatus(application.getId(), CCAP, DELIVERY_FAILED);
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

    // Uncomment the following line to regenerate the test files (useful if the files or cover page have changed)
//         writeByteArrayToFile(captor.getAllValues().get(0).getFileBytes(), "src/test/resources/shiba+file.pdf");
//         writeByteArrayToFile(captor.getAllValues().get(1).getFileBytes(), "src/test/resources/test-uploaded-pdf-with-coverpage.pdf");
    // Assert that converted file contents are as expected
    verifyGeneratedPdf(captor.getAllValues().get(0).getFileBytes(), "shiba+file.pdf");
    verifyGeneratedPdf(captor.getAllValues().get(1).getFileBytes(),
        "test-uploaded-pdf-with-coverpage.pdf");
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
    ApplicationFile pdfApplicationFile = new ApplicationFile(null, "someFile.pdf");
    verify(mnitClient, never()).send(pdfApplicationFile, countyMap.get(Olmsted),
        application.getId(), CCAP, FULL);
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

    verify(applicationRepository).updateStatus(application.getId(), UPLOADED_DOC, SENDING);
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