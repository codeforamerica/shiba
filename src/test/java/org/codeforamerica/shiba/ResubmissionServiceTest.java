package org.codeforamerica.shiba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.application.Status.DELIVERED_BY_EMAIL;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.*;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.TribalNationConfiguration;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ResubmissionServiceTest {

  private final String APP_ID = "myappid";
  private final String DEFAULT_EMAIL = "olmsted@example.com";
  private final String ANOKA_EMAIL = "anoka@example.com";
  private final String MILLE_LACS_BAND_EMAIL = "help+dev@mnbenefits.org";

  private final CountyMap<CountyRoutingDestination> countyMap = new CountyMap<>();
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private MailGunEmailClient emailClient;
  @Mock
  private PdfGenerator pdfGenerator;
  @Mock
  private RoutingDecisionService routingDecisionService;
  private ResubmissionService resubmissionService;
  @Mock
  private ApplicationStatusRepository applicationStatusRepository;
  @Mock
  private PageEventPublisher pageEventPublisher;
  @Mock
  private  FilenameGenerator filenameGenerator ;

  @BeforeEach
  void setUp() {
    countyMap.setDefaultValue(CountyRoutingDestination.builder()
        .dhsProviderId("defaultDhsProviderId")
        .email(DEFAULT_EMAIL) // TODO test other counties besides DEFAULT
        .build());
    String OLMSTED_EMAIL = "olmsted@example.com";
    countyMap.setCounties(Map.of(
        Anoka, CountyRoutingDestination.builder().county(Anoka).email(ANOKA_EMAIL).build(),
        Olmsted, CountyRoutingDestination.builder().county(Olmsted).email(OLMSTED_EMAIL).build()
    ));
    Map<String, TribalNationRoutingDestination> tribalNations = new TribalNationConfiguration().localTribalNations();
    routingDecisionService = new RoutingDecisionService(tribalNations, countyMap, mock(
        FeatureFlagConfiguration.class));
    FeatureFlagConfiguration featureFlagConfiguration = new FeatureFlagConfiguration(Map.of());
    filenameGenerator = new FilenameGenerator(countyMap);
    resubmissionService = new ResubmissionService(applicationRepository, emailClient,
        pdfGenerator, routingDecisionService, applicationStatusRepository, pageEventPublisher, featureFlagConfiguration);
  }

  @Test
  void itResubmitsCafs() {
    Application application = Application.builder().id(APP_ID).county(Olmsted).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit())
        .thenReturn(List.of(new ApplicationStatus(APP_ID, CAF, "Olmsted", DELIVERY_FAILED, "")));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(eq(application), eq(CAF), eq(Recipient.CASEWORKER), any())).thenReturn(applicationFile);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient).resubmitFailedEmail(DEFAULT_EMAIL, CAF, applicationFile, application);
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, "Olmsted",
        Status.DELIVERED_BY_EMAIL, "");
  }

  @Test
  void itResubmitsCafsToTribalNationsOnly() {
    Application application = Application.builder().id(APP_ID).county(Olmsted).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit())
        .thenReturn(List.of(
            new ApplicationStatus(APP_ID, CAF, MILLE_LACS_BAND_OF_OJIBWE, DELIVERY_FAILED, "")));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(eq(application), eq(CAF), eq(Recipient.CASEWORKER), any())).thenReturn(applicationFile);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient, times(1)).resubmitFailedEmail(any(), any(), any(),
        any());
    verify(emailClient).resubmitFailedEmail(MILLE_LACS_BAND_EMAIL, CAF, applicationFile,
        application);
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, MILLE_LACS_BAND_OF_OJIBWE,
        Status.DELIVERED_BY_EMAIL, "");
  }

  @Test
  void itResubmitsCafsToTribalNationsAndCounties() {
    Application application = Application.builder().id(APP_ID).county(Anoka).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit())
        .thenReturn(List.of(
            new ApplicationStatus(APP_ID, CAF, "Anoka", DELIVERY_FAILED, ""),
            new ApplicationStatus(APP_ID, CAF, MILLE_LACS_BAND_OF_OJIBWE, DELIVERY_FAILED, "")));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(eq(application), eq(CAF), eq(Recipient.CASEWORKER), any())).thenReturn(applicationFile);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient).resubmitFailedEmail(MILLE_LACS_BAND_EMAIL, CAF, applicationFile,
        application);
    verify(emailClient).resubmitFailedEmail(ANOKA_EMAIL, CAF, applicationFile, application);
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, "Anoka", Status.DELIVERED_BY_EMAIL, "");
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, MILLE_LACS_BAND_OF_OJIBWE,
        Status.DELIVERED_BY_EMAIL, "");
  }

  @Test
  void itShouldMarkDeliveryFailedWhenApplicationFailsToSendToEitherCountyOrTribalNation() {
    Application application = Application.builder().id(APP_ID).county(Anoka).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit())
        .thenReturn(List.of(
            new ApplicationStatus(APP_ID, CAF, "Anoka", DELIVERY_FAILED, ""),
            new ApplicationStatus(APP_ID, CAF, MILLE_LACS_BAND_OF_OJIBWE, DELIVERY_FAILED, "")));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(eq(application), eq(CAF), eq(Recipient.CASEWORKER), any())).thenReturn(applicationFile);

    doNothing().when(emailClient)
        .resubmitFailedEmail(ANOKA_EMAIL, CAF, applicationFile, application);
    doThrow(new RuntimeException()).when(emailClient)
        .resubmitFailedEmail(MILLE_LACS_BAND_EMAIL, CAF, applicationFile, application);

    resubmissionService.resubmitFailedApplications();
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, "Anoka", DELIVERED_BY_EMAIL, "");
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, MILLE_LACS_BAND_OF_OJIBWE,
        RESUBMISSION_FAILED, "");
  }

  @Test
  void itResubmitsUploadedDocuments() {
    ApplicationData applicationData = new ApplicationData();
    MockMultipartFile image = new MockMultipartFile("image", "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg", "fileName.txt");
    applicationData.addUploadedDoc(image, "someS3FilePath2", "someDataUrl2", "image/jpeg", "fileName1.txt");

    Application application = Application.builder().id(APP_ID).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit())
        .thenReturn(
            List.of(new ApplicationStatus(APP_ID, UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName.txt"),
                new ApplicationStatus(APP_ID, UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName1.txt")));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile1 = new ApplicationFile("test".getBytes(), "fileName.txt");
    ApplicationFile applicationFile2 = new ApplicationFile("test".getBytes(), "fileName1.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generateCoverPageForUploadedDocs(any()))
        .thenReturn(coverPage);
    var uploadedDocs = applicationData.getUploadedDocs();
    when(pdfGenerator.generateForUploadedDocument(eq(uploadedDocs.get(0)), eq(0), eq(application), eq(coverPage), any()))
        .thenReturn(applicationFile1);
    when(pdfGenerator.generateForUploadedDocument(eq(uploadedDocs.get(1)), eq(0), eq(application), eq(coverPage), any()))
        .thenReturn(applicationFile2);

    resubmissionService.resubmitFailedApplications();

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(emailClient, times(2))
        .resubmitFailedEmail(eq(DEFAULT_EMAIL), eq(UPLOADED_DOC), captor.capture(),
            eq(application));

    List<ApplicationFile> applicationFiles = captor.getAllValues();
    assertThat(applicationFiles)
        .containsExactlyElementsOf(List.of(applicationFile1, applicationFile2));
    verify(applicationStatusRepository).createOrUpdate(APP_ID, UPLOADED_DOC, "Olmsted",
        Status.DELIVERED_BY_EMAIL, "fileName.txt");
    verify(applicationStatusRepository).createOrUpdate(APP_ID, UPLOADED_DOC, "Olmsted",
        Status.DELIVERED_BY_EMAIL, "fileName1.txt");
  }

  @Test
  void itResubmitsCCAPAndUploadedDocuments() {
    var applicationData = new ApplicationData();
    applicationData
        .addUploadedDoc(new MockMultipartFile("image", "test".getBytes()), "someS3FilePath",
            "someDataUrl", "image/jpeg", "fileName.txt");
    var application = Application.builder().id(APP_ID).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit()).thenReturn(List.of(
        new ApplicationStatus(APP_ID, CCAP, "Olmsted", DELIVERY_FAILED, ""),
        new ApplicationStatus(APP_ID, UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName.txt")));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    var uploadedDocWithCoverPageFile = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generateCoverPageForUploadedDocs(application)).thenReturn(coverPage);
    UploadedDocument firstUploadedDoc = applicationData.getUploadedDocs().get(0);
    when(pdfGenerator.generateForUploadedDocument(eq(firstUploadedDoc), eq(0), eq(application),
        eq(coverPage), any())).thenReturn(uploadedDocWithCoverPageFile);

    var ccapFile = new ApplicationFile("fileContent".getBytes(), "");
    when(pdfGenerator.generate(eq(application), eq(CCAP), eq(Recipient.CASEWORKER), any())).thenReturn(ccapFile);

    resubmissionService.resubmitFailedApplications();

    // Make sure we sent it
    var applicationFileCaptor = ArgumentCaptor.forClass(ApplicationFile.class);
    var documentCaptor = ArgumentCaptor.forClass(Document.class);
    verify(emailClient, times(2)).resubmitFailedEmail(eq(DEFAULT_EMAIL), documentCaptor.capture(),
        applicationFileCaptor.capture(), eq(application));
    assertThat(applicationFileCaptor.getAllValues())
        .containsExactlyInAnyOrder(uploadedDocWithCoverPageFile, ccapFile);
    assertThat(documentCaptor.getAllValues()).containsExactlyInAnyOrder(UPLOADED_DOC, CCAP);

    // make sure we updated the status
    var applicationRepositoryDocumentCaptor = ArgumentCaptor.forClass(Document.class);
    verify(applicationStatusRepository).createOrUpdate(eq(APP_ID), applicationRepositoryDocumentCaptor.capture(), eq("Olmsted"),
            eq(Status.DELIVERED_BY_EMAIL), eq(""));
    verify(applicationStatusRepository).createOrUpdate(eq(APP_ID), applicationRepositoryDocumentCaptor.capture(), eq("Olmsted"),
        eq(Status.DELIVERED_BY_EMAIL), eq("fileName.txt"));
    assertThat(applicationRepositoryDocumentCaptor.getAllValues())
        .containsExactlyInAnyOrder(UPLOADED_DOC, CCAP);
  }

  @Test
  void itUpdatesTheStatusWhenResubmissionIsUnsuccessful() {
    var applicationData = new ApplicationData();
    applicationData
        .addUploadedDoc(new MockMultipartFile("image", "test".getBytes()), "someS3FilePath",
            "someDataUrl", "image/jpeg", "fileName.txt");
    var application = Application.builder().id(APP_ID).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit())
        .thenReturn(
            List.of(new ApplicationStatus(APP_ID, UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName.txt")));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    var uploadedDocWithCoverPageFile = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generateCoverPageForUploadedDocs(any()))
    .thenReturn(coverPage);
    when(pdfGenerator
        .generateForUploadedDocument(eq(applicationData.getUploadedDocs().get(0)), eq(0), eq(application),
            eq(coverPage), any())).thenThrow(RuntimeException.class);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient, never())
        .resubmitFailedEmail(DEFAULT_EMAIL, UPLOADED_DOC, uploadedDocWithCoverPageFile,
            application);
    verify(applicationStatusRepository).createOrUpdate(APP_ID, UPLOADED_DOC, "Olmsted",
        RESUBMISSION_FAILED, "fileName.txt");
  }

  @Test
  void shouldUpdateStatusToResubmissionFailedForUnknownCounty() {
    Application application = Application.builder().id(APP_ID).county(Anoka).build();
    when(applicationStatusRepository.getDocumentStatusToResubmit())
        .thenReturn(List.of(
            new ApplicationStatus(APP_ID, CAF, "Anoka", DELIVERY_FAILED, ""),
            new ApplicationStatus(APP_ID, CAF, "Invalid County", DELIVERY_FAILED, ""),
            new ApplicationStatus(APP_ID, CAF, "Olmsted", DELIVERY_FAILED, "")
        ));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(eq(application), eq(CAF), eq(Recipient.CASEWORKER),any())).thenReturn(applicationFile);

    resubmissionService.resubmitFailedApplications();

    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, "Anoka", DELIVERED_BY_EMAIL, "");
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, "Invalid County",
        RESUBMISSION_FAILED, "");
    verify(applicationStatusRepository).createOrUpdate(APP_ID, CAF, "Olmsted", DELIVERED_BY_EMAIL, "");
  }
}
