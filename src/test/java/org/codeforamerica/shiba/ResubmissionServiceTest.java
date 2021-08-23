package org.codeforamerica.shiba;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.application.Status.RESUBMISSION_FAILED;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.documents.CombinedDocumentRepositoryService;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
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

  final String defaultEmail = "defaultEmail";
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private MailGunEmailClient emailClient;
  @Mock
  private PdfGenerator pdfGenerator;
  @Mock
  private CombinedDocumentRepositoryService combinedDocumentRepositoryService;
  private CountyMap<MnitCountyInformation> countyMap = new CountyMap<>();
  private ResubmissionService resubmissionService;

  @BeforeEach
  void setUp() {
    countyMap.setDefaultValue(MnitCountyInformation.builder()
        .folderId("defaultFolderId")
        .dhsProviderId("defaultDhsProviderId")
        .email(defaultEmail)
        .build());
    resubmissionService = new ResubmissionService(applicationRepository, emailClient, countyMap,
        pdfGenerator, combinedDocumentRepositoryService);
  }

  @Test
  void itResubmitsCafs() {
    String appId = "myappid";
    Application application = Application.builder().id(appId).county(Olmsted).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(CAF, List.of(appId)));
    when(applicationRepository.find(appId)).thenReturn(application);

    ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(application, CAF, Recipient.CASEWORKER)).thenReturn(applicationFile);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient)
        .resubmitFailedEmail(defaultEmail, CAF, applicationFile, application, ENGLISH);
    verify(applicationRepository).updateStatus(appId, CAF, Status.DELIVERED);
  }

  @Test
  void itResubmitsUploadedDocuments() {
    String appId = "myappid";
    ApplicationData applicationData = new ApplicationData();
    MockMultipartFile image = new MockMultipartFile("image", "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");
    applicationData.addUploadedDoc(image, "someS3FilePath2", "someDataUrl2", "image/jpeg");

    Application application = Application.builder().id(appId).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(UPLOADED_DOC, List.of(appId)));
    when(applicationRepository.find(appId)).thenReturn(application);

    ApplicationFile applicationFile1 = new ApplicationFile("test".getBytes(), "fileName.txt");
    ApplicationFile applicationFile2 = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generate(application, UPLOADED_DOC, Recipient.CASEWORKER))
        .thenReturn(new ApplicationFile(coverPage, "coverPage"));
    var uploadedDocs = applicationData.getUploadedDocs();
    when(pdfGenerator.generateForUploadedDocument(uploadedDocs.get(0), 0, application, coverPage))
        .thenReturn(applicationFile1);
    when(pdfGenerator.generateForUploadedDocument(uploadedDocs.get(1), 1, application, coverPage))
        .thenReturn(applicationFile2);

    resubmissionService.resubmitFailedApplications();

    ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
    verify(emailClient, times(2))
        .resubmitFailedEmail(eq(defaultEmail), eq(UPLOADED_DOC), captor.capture(), eq(application),
            eq(ENGLISH));

    List<ApplicationFile> applicationFiles = captor.getAllValues();
    assertThat(applicationFiles)
        .containsExactlyElementsOf(List.of(applicationFile1, applicationFile2));
    verify(applicationRepository).updateStatus(appId, UPLOADED_DOC, Status.DELIVERED);
  }

  @Test
  void itResubmitsCCAPAndUploadedDocuments() {
    var appId = "myappid";
    var applicationData = new ApplicationData();
    applicationData
        .addUploadedDoc(new MockMultipartFile("image", "test".getBytes()), "someS3FilePath",
            "someDataUrl", "image/jpeg");
    var application = Application.builder().id(appId).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(CCAP, List.of(appId), UPLOADED_DOC, List.of(appId)));
    when(applicationRepository.find(appId)).thenReturn(application);

    var uploadedDocWithCoverPageFile = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generate(application, UPLOADED_DOC, Recipient.CASEWORKER))
        .thenReturn(new ApplicationFile(coverPage, "coverPage"));
    when(pdfGenerator
        .generateForUploadedDocument(applicationData.getUploadedDocs().get(0), 0, application,
            coverPage)).thenReturn(uploadedDocWithCoverPageFile);

    var ccapFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
    when(pdfGenerator.generate(application, CCAP, Recipient.CASEWORKER)).thenReturn(ccapFile);

    resubmissionService.resubmitFailedApplications();

    // Make sure we sent it
    var applicationFileCaptor = ArgumentCaptor.forClass(ApplicationFile.class);
    var documentCaptor = ArgumentCaptor.forClass(Document.class);
    verify(emailClient, times(2)).resubmitFailedEmail(eq(defaultEmail), documentCaptor.capture(),
        applicationFileCaptor.capture(), eq(application), eq(ENGLISH));
    assertThat(applicationFileCaptor.getAllValues())
        .containsExactlyInAnyOrder(uploadedDocWithCoverPageFile, ccapFile);
    assertThat(documentCaptor.getAllValues()).containsExactlyInAnyOrder(UPLOADED_DOC, CCAP);

    // make sure we updated the status
    var applicationRepositoryDocumentCaptor = ArgumentCaptor.forClass(Document.class);
    verify(applicationRepository, times(2))
        .updateStatus(eq(appId), applicationRepositoryDocumentCaptor.capture(),
            eq(Status.DELIVERED));
    assertThat(applicationRepositoryDocumentCaptor.getAllValues())
        .containsExactlyInAnyOrder(UPLOADED_DOC, CCAP);
  }


  @Test
  void itUpdatesTheStatusWhenResubmissionIsUnsuccessful() {
    var appId = "myappid";
    var applicationData = new ApplicationData();
    applicationData
        .addUploadedDoc(new MockMultipartFile("image", "test".getBytes()), "someS3FilePath",
            "someDataUrl", "image/jpeg");
    var application = Application.builder().id(appId).county(Olmsted)
        .applicationData(applicationData).build();
    when(applicationRepository.getApplicationIdsToResubmit())
        .thenReturn(Map.of(UPLOADED_DOC, List.of(appId)));
    when(applicationRepository.find(appId)).thenReturn(application);

    var uploadedDocWithCoverPageFile = new ApplicationFile("test".getBytes(), "fileName.txt");
    var coverPage = "someCoverPageText".getBytes();
    when(pdfGenerator.generate(application, UPLOADED_DOC, Recipient.CASEWORKER))
        .thenReturn(new ApplicationFile(coverPage, "coverPage"));
    when(pdfGenerator
        .generateForUploadedDocument(applicationData.getUploadedDocs().get(0), 0, application,
            coverPage)).thenThrow(RuntimeException.class);

    resubmissionService.resubmitFailedApplications();

    verify(emailClient, never())
        .resubmitFailedEmail(defaultEmail, UPLOADED_DOC, uploadedDocWithCoverPageFile, application,
            ENGLISH);
    verify(applicationRepository).updateStatus(appId, UPLOADED_DOC, RESUBMISSION_FAILED);
  }
}
