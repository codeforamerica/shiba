package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DocumentUploadEmailServiceTest {

  @Autowired
  private DocumentUploadEmailService documentUploadEmailService;

  @Autowired
  private ApplicationRepository applicationRepository;

  @MockBean
  private EmailClient emailClient;

  @BeforeEach
  void setUp() {
  }

  @Test
  void sendDocumentUploadEmails() {
    Application appThatShouldTriggerEmail = saveApplicationThatNeedsDocumentUploadEmail();
    Application appThatIsTooOld = saveApplicationThatIsTooOld();
    Application appWithDocumentUploads = saveApplicationWithDocumentUploads();
    Application appThatAlreadyHadEmailSent = saveApplicationThatAlreadyHadEmailSent();

    documentUploadEmailService.sendDocumentUploadEmails();

    assertThat(
        applicationRepository.find(appThatShouldTriggerEmail.getId())
    ).hasFieldOrPropertyWithValue("docUploadEmailStatus", Status.DELIVERED);

    assertThat(
        applicationRepository.find(appThatIsTooOld.getId()).getDocUploadEmailStatus()
    ).isNull();

    assertThat(
        applicationRepository.find(appWithDocumentUploads.getId()).getDocUploadEmailStatus()
    ).isNull();

    assertThat(
        applicationRepository.find(appThatAlreadyHadEmailSent.getId())
    ).hasFieldOrPropertyWithValue("docUploadEmailStatus", Status.DELIVERED);

    verify(emailClient, times(1)).sendEmail(any(), any(), any(), any(), any());
  }

  private Application saveApplicationThatNeedsDocumentUploadEmail() {
    Application appThatShouldTriggerEmail = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20))
        .county(County.Anoka)
        .id("abc123")
        .applicationData(new ApplicationData())
        .docUploadEmailStatus(null)
        .build();
    applicationRepository.save(appThatShouldTriggerEmail);
    return appThatShouldTriggerEmail;
  }

  private Application saveApplicationThatAlreadyHadEmailSent() {
    Application appThatShouldTriggerEmail = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20))
        .county(County.Anoka)
        .id("abc123")
        .applicationData(new ApplicationData())
        .docUploadEmailStatus(Status.DELIVERED)
        .build();
    applicationRepository.save(appThatShouldTriggerEmail);
    return appThatShouldTriggerEmail;
  }


  private Application saveApplicationThatIsTooOld() {
    Application appThatIsTooOld = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(49))
        .county(County.Anoka)
        .id("def456")
        .applicationData(new ApplicationData())
        .docUploadEmailStatus(null)
        .build();
    applicationRepository.save(appThatIsTooOld);
    return appThatIsTooOld;
  }


  private Application saveApplicationWithDocumentUploads() {
    ApplicationData appDataWithUploadedDocument = new ApplicationData();
    appDataWithUploadedDocument.addUploadedDoc(new MockMultipartFile("image", "someImage.jpg",
            MediaType.IMAGE_JPEG_VALUE, "test".getBytes()), "someS3FilePath", "someDataUrl",
        "image/jpeg");
    Application appWithDocumentUploads = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(13))
        .county(County.Anoka)
        .id("ghi789")
        .applicationData(appDataWithUploadedDocument)
        .docUploadEmailStatus(null)
        .build();
    applicationRepository.save(appWithDocumentUploads);
    return appWithDocumentUploads;
  }
}