package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
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

  private final String CLIENT_EMAIL = "client@example.com";

  @Autowired
  private DocumentUploadEmailService documentUploadEmailService;

  @Autowired
  private ApplicationRepository applicationRepository;

  @MockBean
  private EmailClient emailClient;

  @Test
  void sendDocumentUploadEmails() {
    Application appThatShouldTriggerEmail = saveApplicationThatNeedsDocumentUploadEmail();
    Application appThatIsTooOld = saveApplicationThatIsTooOld();
    Application appWithDocumentUploads = saveApplicationWithDocumentUploads();
    Application appThatAlreadyHadEmailSent = saveApplicationThatAlreadyHadEmailSent();
    Application laterDocsApplication = saveLaterDocsApplication();
    Application appWithoutEmail = saveApplicationWithoutEmail();
    Application appThatOptedOutOfEmails = saveApplicationThatOptedOutOfEmails();
    Application appWithoutDocRecommendations = saveApplicationWithoutDocRecommendations();

    documentUploadEmailService.sendDocumentUploadEmailReminders();

    assertEmailSent(appThatShouldTriggerEmail);
    assertEmailDidNotSend(appThatIsTooOld);
    assertEmailDidNotSend(appWithDocumentUploads);
    assertEmailDidNotSend(laterDocsApplication);
    assertEmailSent(appThatAlreadyHadEmailSent);
    assertEmailDidNotSend(appWithoutEmail);
    assertEmailDidNotSend(appWithoutDocRecommendations);
    assertEmailDidNotSend(appThatOptedOutOfEmails);
    verify(emailClient, times(1)).sendEmail(
        eq("[Action Required] Upload Documents To Your MNbenefits Application"),
        eq("sender@email.org"),
        eq(CLIENT_EMAIL),
        eq("<html><body>Remember to upload documents on <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> to support your MN Benefits application. You can use your phone to take or upload pictures, or use your computer to upload documents.<br>If you have them, you should upload the following documents:<br><ul><li><strong>Proof of Income:</strong> A document with employer and employee names and your total pre-tax income from the last 30 days (or total hours worked and rate of pay). Example: Paystubs</li></ul>If you have already uploaded these documents, you can ignore this reminder.</body></html>"));
  }

  private void assertEmailDidNotSend(Application appThatIsTooOld) {
    assertThat(
        applicationRepository.find(appThatIsTooOld.getId()).getDocUploadEmailStatus()
    ).isNull();
  }

  private void assertEmailSent(Application application) {
    assertThat(
        applicationRepository.find(application.getId())
    ).hasFieldOrPropertyWithValue("docUploadEmailStatus", Status.DELIVERED);
  }

  private Application saveApplicationThatNeedsDocumentUploadEmail() {
    Application appThatShouldTriggerEmail = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20))
        .county(County.Anoka)
        .id("qrt386")
        .applicationData(getApplicationDataWithEmailAndDocRecommendations())
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(appThatShouldTriggerEmail);
    return appThatShouldTriggerEmail;
  }

  private Application saveApplicationThatOptedOutOfEmails() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of(CLIENT_EMAIL),
            "phoneOrEmail", List.of("PHONE")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    Application app = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20))
        .county(County.Anoka)
        .id("stu889")
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(app);
    return app;
  }

  private Application saveApplicationWithoutEmail() {
    Application appThatShouldTriggerEmail = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20))
        .county(County.Anoka)
        .id("abc123")
        .applicationData(new ApplicationData())
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(appThatShouldTriggerEmail);
    return appThatShouldTriggerEmail;
  }

  private Application saveApplicationThatAlreadyHadEmailSent() {
    ApplicationData applicationData = getApplicationDataWithEmailAndDocRecommendations();
    Application app = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20))
        .county(County.Anoka)
        .id("awe134")
        .applicationData(applicationData)
        .docUploadEmailStatus(Status.DELIVERED)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(app);
    return app;
  }

  private Application saveApplicationWithoutDocRecommendations() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of(CLIENT_EMAIL),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "false")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    Application app = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20))
        .county(County.Anoka)
        .id("xyz381")
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(app);
    return app;
  }

  private ApplicationData getApplicationDataWithEmailAndDocRecommendations() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of(CLIENT_EMAIL),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    return applicationData;
  }

  private Application saveApplicationThatIsTooOld() {
    Application appThatIsTooOld = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(49))
        .county(County.Anoka)
        .id("def456")
        .applicationData(getApplicationDataWithEmailAndDocRecommendations())
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(appThatIsTooOld);
    return appThatIsTooOld;
  }

  private Application saveApplicationWithDocumentUploads() {
    ApplicationData applicationData = getApplicationDataWithEmailAndDocRecommendations();
    applicationData.addUploadedDoc(new MockMultipartFile("image", "someImage.jpg",
            MediaType.IMAGE_JPEG_VALUE, "test".getBytes()), "someS3FilePath", "someDataUrl",
        "image/jpeg");
    Application app = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(13))
        .county(County.Anoka)
        .id("ghi789")
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(app);
    return app;
  }

  private Application saveLaterDocsApplication() {
    ApplicationData applicationData = getApplicationDataWithEmailAndDocRecommendations();
    applicationData.addUploadedDoc(new MockMultipartFile("image", "someImage.jpg",
            MediaType.IMAGE_JPEG_VALUE, "test".getBytes()), "someS3FilePath", "someDataUrl",
        "image/jpeg");
    applicationData.setFlow(FlowType.LATER_DOCS);

    Application app = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(13))
        .county(County.Anoka)
        .id("jkl912")
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.LATER_DOCS)
        .build();
    applicationRepository.save(app);
    return app;
  }
}