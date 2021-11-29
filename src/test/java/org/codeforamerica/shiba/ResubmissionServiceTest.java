package org.codeforamerica.shiba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.mnit.TribalNationConfiguration;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
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
  private final String OLMSTED_EMAIL = "olmsted@example.com";
  private final String MILLE_LACS_BAND_EMAIL = "help+dev@mnbenefits.org";

  private final CountyMap<CountyRoutingDestination> countyMap = new CountyMap<>();
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private MailGunEmailClient emailClient;
  @Mock
  private PdfGenerator pdfGenerator;
  @Mock
  private DocumentRepository documentRepository;
  @Mock
  private RoutingDecisionService routingDecisionService;
  private Map<String, TribalNationRoutingDestination> tribalNations;
  private ResubmissionService resubmissionService;
  private List<RoutingDestination> routingDestinations;

  @BeforeEach
  void setUp() {
    countyMap.setDefaultValue(CountyRoutingDestination.builder()
        .folderId("defaultFolderId")
        .dhsProviderId("defaultDhsProviderId")
        .email(DEFAULT_EMAIL) // TODO test other counties besides DEFAULT
        .build());
    countyMap.setCounties(Map.of(
        Anoka, CountyRoutingDestination.builder().email(ANOKA_EMAIL).build()
    ));
    tribalNations = new TribalNationConfiguration().localTribalNations();
    resubmissionService = new ResubmissionService(applicationRepository, emailClient, countyMap,
        pdfGenerator, routingDecisionService);

    routingDestinations = new ArrayList<>();
    routingDestinations.add(
        CountyRoutingDestination.builder().county(Olmsted).email(OLMSTED_EMAIL).build());
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(
        routingDestinations);
  }

  @Test
  void itResubmitsCafs() {
    Application application = Application.builder().id(APP_ID).county(Olmsted).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(CAF, List.of(APP_ID)));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(application, CAF, Recipient.CASEWORKER)).thenReturn(applicationFile);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient).resubmitFailedEmail(DEFAULT_EMAIL, CAF, applicationFile, application);
    verify(applicationRepository).updateStatus(APP_ID, CAF, Status.DELIVERED);
    verify(applicationRepository).updateStatus(APP_ID, CAF, routingDestinations, Status.DELIVERED);
  }

  @Test
  void itResubmitsCafsToTribalNationsOnly() {
    Application application = Application.builder().id(APP_ID).county(Olmsted).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(CAF, List.of(APP_ID)));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(application, CAF, Recipient.CASEWORKER)).thenReturn(applicationFile);

    routingDestinations.remove(0);
    routingDestinations.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE));

    resubmissionService.resubmitFailedApplications();

    verify(emailClient, times(1)).resubmitFailedEmail(any(), any(), any(),
        any());
    verify(emailClient).resubmitFailedEmail(MILLE_LACS_BAND_EMAIL, CAF, applicationFile,
        application);
    verify(applicationRepository).updateStatus(APP_ID, CAF, Status.DELIVERED);
    verify(applicationRepository).updateStatus(APP_ID, CAF, routingDestinations, Status.DELIVERED);
  }

  @Test
  void itResubmitsCafsToTribalNationsAndCounties() {
    Application application = Application.builder().id(APP_ID).county(Anoka).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(CAF, List.of(APP_ID)));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(application, CAF, Recipient.CASEWORKER)).thenReturn(applicationFile);

    routingDestinations.remove(0);
    routingDestinations.add(countyMap.get(Anoka));
    routingDestinations.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE));

    resubmissionService.resubmitFailedApplications();

    verify(emailClient).resubmitFailedEmail(MILLE_LACS_BAND_EMAIL, CAF, applicationFile,
        application);
    verify(emailClient).resubmitFailedEmail(ANOKA_EMAIL, CAF, applicationFile,
        application);
    verify(applicationRepository).updateStatus(APP_ID, CAF, Status.DELIVERED);
    verify(applicationRepository).updateStatus(APP_ID, CAF, routingDestinations, Status.DELIVERED);
  }

  @Test
  void itShouldMarkDeliveryFailedWhenApplicationFailsToSendToEitherCountyOrTribalNation() {
    Application application = Application.builder().id(APP_ID).county(Anoka).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(CAF, List.of(APP_ID)));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(application, CAF, Recipient.CASEWORKER)).thenReturn(applicationFile);

    routingDestinations.remove(0);
    routingDestinations.add(countyMap.get(Anoka));
    routingDestinations.add(tribalNations.get(MILLE_LACS_BAND_OF_OJIBWE));

    doThrow(new RuntimeException()).when(emailClient)
        .resubmitFailedEmail(MILLE_LACS_BAND_EMAIL, CAF, applicationFile,
            application);

    resubmissionService.resubmitFailedApplications();
    verify(applicationRepository).updateStatus(APP_ID, CAF, RESUBMISSION_FAILED);
    verify(applicationRepository).updateStatus(APP_ID, CAF, routingDestinations,
        RESUBMISSION_FAILED);
  }

  @Test
  void itResubmitsUploadedDocuments() {
    ApplicationData applicationData = new ApplicationData();
    MockMultipartFile image = new MockMultipartFile("image", "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");
    applicationData.addUploadedDoc(image, "someS3FilePath2", "someDataUrl2", "image/jpeg");

    Application application = Application.builder().id(APP_ID).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(UPLOADED_DOC, List.of(APP_ID)));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    ApplicationFile applicationFile1 = new ApplicationFile("test".getBytes(), "fileName.txt");
    ApplicationFile applicationFile2 = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generateCoverPageForUploadedDocs(application))
        .thenReturn(coverPage);
    var uploadedDocs = applicationData.getUploadedDocs();
    when(pdfGenerator.generateForUploadedDocument(uploadedDocs.get(0), 0, application, coverPage))
        .thenReturn(applicationFile1);
    when(pdfGenerator.generateForUploadedDocument(uploadedDocs.get(1), 1, application, coverPage))
        .thenReturn(applicationFile2);

    resubmissionService.resubmitFailedApplications();

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(emailClient, times(2))
        .resubmitFailedEmail(eq(DEFAULT_EMAIL), eq(UPLOADED_DOC), captor.capture(),
            eq(application));

    List<ApplicationFile> applicationFiles = captor.getAllValues();
    assertThat(applicationFiles)
        .containsExactlyElementsOf(List.of(applicationFile1, applicationFile2));
    verify(applicationRepository).updateStatus(APP_ID, UPLOADED_DOC, Status.DELIVERED);
    verify(applicationRepository).updateStatus(APP_ID, UPLOADED_DOC, routingDestinations,
        Status.DELIVERED);
  }

  @Test
  void itResubmitsCCAPAndUploadedDocuments() {
    var applicationData = new ApplicationData();
    applicationData
        .addUploadedDoc(new MockMultipartFile("image", "test".getBytes()), "someS3FilePath",
            "someDataUrl", "image/jpeg");
    var application = Application.builder().id(APP_ID).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(CCAP, List.of(APP_ID), UPLOADED_DOC, List.of(APP_ID)));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    var uploadedDocWithCoverPageFile = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generateCoverPageForUploadedDocs(application)).thenReturn(coverPage);
    UploadedDocument firstUploadedDoc = applicationData.getUploadedDocs().get(0);
    when(pdfGenerator.generateForUploadedDocument(firstUploadedDoc, 0, application,
        coverPage)).thenReturn(uploadedDocWithCoverPageFile);

    var ccapFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(application, CCAP, Recipient.CASEWORKER)).thenReturn(ccapFile);

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
    verify(applicationRepository, times(2))
        .updateStatus(eq(APP_ID), applicationRepositoryDocumentCaptor.capture(),
            eq(Status.DELIVERED));
    verify(applicationRepository).updateStatus(APP_ID, CCAP, routingDestinations, Status.DELIVERED);
    assertThat(applicationRepositoryDocumentCaptor.getAllValues())
        .containsExactlyInAnyOrder(UPLOADED_DOC, CCAP);
  }

  @Test
  void itUpdatesTheStatusWhenResubmissionIsUnsuccessful() {
    var applicationData = new ApplicationData();
    applicationData
        .addUploadedDoc(new MockMultipartFile("image", "test".getBytes()), "someS3FilePath",
            "someDataUrl", "image/jpeg");
    var application = Application.builder().id(APP_ID).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(UPLOADED_DOC, List.of(APP_ID)));
    when(applicationRepository.find(APP_ID)).thenReturn(application);

    var uploadedDocWithCoverPageFile = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generate(application, UPLOADED_DOC, Recipient.CASEWORKER))
        .thenReturn(new ApplicationFile(coverPage, "coverPage"));
    when(pdfGenerator
        .generateForUploadedDocument(applicationData.getUploadedDocs().get(0), 0, application,
            coverPage)).thenThrow(RuntimeException.class);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient, never())
        .resubmitFailedEmail(DEFAULT_EMAIL, UPLOADED_DOC, uploadedDocWithCoverPageFile,
            application);
    verify(applicationRepository).updateStatus(APP_ID, UPLOADED_DOC, RESUBMISSION_FAILED);
    verify(applicationRepository).updateStatus(APP_ID, UPLOADED_DOC, routingDestinations,
        RESUBMISSION_FAILED);
  }
}
