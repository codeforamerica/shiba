package org.codeforamerica.shiba.pages.emails;

import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.matching.MultipartValuePattern.MatchingType.ANY;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.UNDETERMINED;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.MultipartValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;

@SuppressWarnings("unchecked")
@SpringBootTest(webEnvironment = NONE, properties = {
    "spring.profiles.active=test"
})
@ActiveProfiles("test")
class MailGunEmailClientTest {

  final String mailGunApiKey = "someMailGunApiKey";
  final String senderEmail = "someSenderEmail";
  final String securityEmail = "someSecurityEmail";
  final String auditEmail = "someAuditEmail";
  final String hennepinEmail = "someHennepinEmail";
  final int MAX_ATTACHMENT_SIZE = 100;
  MailGunEmailClient mailGunEmailClient;
  EmailContentCreator emailContentCreator;
  WireMockServer wireMockServer;
  @MockBean
  private ClientRegistrationRepository springSecurityFilterChain;
  PdfGenerator pdfGenerator = mock(PdfGenerator.class);
  ApplicationRepository applicationRepository;
  int port;
  BasicCredentials credentials;
  List<String> programs;
  CcapExpeditedEligibility ccapExpeditedEligibility = UNDETERMINED;
  @Autowired
  private MessageSource messageSource;
  @Value("${spring.profiles.active}")
  private String activeProfile;

  @BeforeEach
  void setUp() {
    emailContentCreator = mock(EmailContentCreator.class);

    WireMockConfiguration options = WireMockConfiguration.wireMockConfig().dynamicPort();
    wireMockServer = new WireMockServer(options);
    wireMockServer.start();
    port = wireMockServer.port();
    applicationRepository = mock(ApplicationRepository.class);
    WireMock.configureFor(port);
    mailGunEmailClient = new MailGunEmailClient(
        senderEmail,
        securityEmail,
        auditEmail,
        hennepinEmail,
        "http://localhost:" + port,
        mailGunApiKey,
        emailContentCreator,
        false,
        MAX_ATTACHMENT_SIZE,
        activeProfile,
        applicationRepository,
        messageSource);
    programs = List.of(Program.SNAP);
    credentials = new BasicCredentials("api", mailGunApiKey);
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void sendsEmailToTheRecipient() {
    var applicationData = new ApplicationData();
    String recipientEmail = "someRecipient";
    String emailContent = "content";
    SnapExpeditedEligibility snapExpeditedEligibility = ELIGIBLE;
    CcapExpeditedEligibility ccapExpeditedEligibility = CcapExpeditedEligibility.ELIGIBLE;
    String confirmationId = "someConfirmationId";
    when(emailContentCreator.createFullClientConfirmationEmail(applicationData,
        confirmationId,
        programs,
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        ENGLISH)).thenReturn(emailContent);

    wireMockServer.stubFor(post(anyUrl())
        .willReturn(aResponse().withStatus(200)));

    String fileContent = "someContent";
    String fileName = "someFileName";
    mailGunEmailClient.sendConfirmationEmail(applicationData,
        recipientEmail,
        confirmationId,
        List.of(Program.SNAP),
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        List.of(new ApplicationFile(fileContent.getBytes(), fileName)), ENGLISH);

    wireMockServer.verify(postToMailgun()
        .withBasicAuth(credentials)
        .withRequestBodyPart(requestBodyPart("from", senderEmail))
        .withRequestBodyPart(requestBodyPart("to", recipientEmail))
        .withRequestBodyPart(requestBodyPart("subject", "We received your MNbenefits application"))
        .withRequestBodyPart(requestBodyPart("html", emailContent))
        .withRequestBodyPart(attachment(String.format("filename=\"%s\"", fileName), fileContent))
    );
  }

  @Test
  void sendsNextStepsEmail() {
    var applicationData = new ApplicationData();
    String recipientEmail = "someRecipient";
    String emailContent = "content";
    SnapExpeditedEligibility snapExpeditedEligibility = ELIGIBLE;
    CcapExpeditedEligibility ccapExpeditedEligibility = CcapExpeditedEligibility.ELIGIBLE;
    String confirmationId = "someConfirmationId";
    when(emailContentCreator.createNextStepsEmail(programs,
        snapExpeditedEligibility, ccapExpeditedEligibility, ENGLISH)).thenReturn(emailContent);

    wireMockServer.stubFor(post(anyUrl())
        .willReturn(aResponse().withStatus(200)));

    String fileContent = "someContent";
    String fileName = "someFileName";
    mailGunEmailClient.sendNextStepsEmail(applicationData,
        recipientEmail,
        confirmationId,
        List.of(Program.SNAP),
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        List.of(new ApplicationFile(fileContent.getBytes(), fileName)), ENGLISH);

    wireMockServer.verify(postToMailgun()
        .withBasicAuth(credentials)
        .withRequestBodyPart(requestBodyPart("from", senderEmail))
        .withRequestBodyPart(requestBodyPart("to", recipientEmail))
        .withRequestBodyPart(requestBodyPart("subject", "Next Steps: Your MNBenefits Application"))
        .withRequestBodyPart(requestBodyPart("html", emailContent))
    );
  }

  @Test
  void sendsHennepinConsolidatedSmallDocUploadsEmail() {
    wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));
    var phoneNumber = "(603) 879-1111";
    var email = "jane@example.com";
    var applicationData = new TestApplicationDataBuilder()
        .withPersonalInfo()
        .withPageData("contactInfo", "phoneNumber", phoneNumber)
        .withPageData("contactInfo", "email", email)
        .build();

    ApplicationFile testFile = new ApplicationFile("testfile".getBytes(), "");
    UploadedDocument doc1 = new UploadedDocument("somefile1", "", "", "", 1000);
    UploadedDocument doc2 = new UploadedDocument("somefile2", "", "", "", 1000);
    applicationData.setUploadedDocs(List.of(doc1, doc2));
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Hennepin)
        .timeToComplete(null)
        .build();
    var emailContent = "content";
    when(emailContentCreator.createHennepinDocUploadsHTML(anyMap())).thenReturn(emailContent);

    mailGunEmailClient.sendHennepinDocUploadsEmails(application, List.of(testFile, testFile));
    verify(applicationRepository).updateStatus("someId", UPLOADED_DOC, "Hennepin",
        Status.DELIVERED);

    wireMockServer.verify(1, postToMailgun()
        .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
        .withRequestBodyPart(requestBodyPart("from", senderEmail))
        .withRequestBodyPart(requestBodyPart("to", hennepinEmail))
        .withRequestBodyPart(requestBodyPart("html", emailContent))
        .withRequestBodyPart(aMultipart()
            .withName("subject")
            .withHeader(CONTENT_TYPE, containing(TEXT_PLAIN_VALUE))
            .withBody(containing("Verification docs for Jane Doe"))
            .matchingType(ANY)
            .build())
    );

    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor
        .forClass(Map.class);
    verify(emailContentCreator).createHennepinDocUploadsHTML(captor.capture());
    Map<String, String> actual = captor.getValue();
    assertThat(actual).containsAllEntriesOf(Map.of(
        "name", "Jane Doe",
        "dob", "10/04/2020",
        "last4SSN", "6789",
        "phoneNumber", phoneNumber,
        "email", email));
  }

  @Test
  void sendsHennepinIndividualLargeDocUploadsEmail() {
    wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));
    var phoneNumber = "(603) 879-1111";
    var email = "jane@example.com";
    var applicationData = new TestApplicationDataBuilder()
        .withPersonalInfo()
        .withPageData("contactInfo", "phoneNumber", phoneNumber)
        .withPageData("contactInfo", "email", email)
        .build();

    ApplicationFile testFile = new ApplicationFile(new byte[MAX_ATTACHMENT_SIZE - 1], "");
    UploadedDocument doc1 = new UploadedDocument("somefile1", "", "", "",
        MAX_ATTACHMENT_SIZE - 1);
    UploadedDocument doc2 = new UploadedDocument("somefile2", "", "", "",
        MAX_ATTACHMENT_SIZE - 1);
    applicationData.setUploadedDocs(List.of(doc1, doc2));
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Hennepin)
        .timeToComplete(null)
        .build();
    var emailContent = "content";
    when(emailContentCreator.createHennepinDocUploadsHTML(anyMap())).thenReturn(emailContent);

    mailGunEmailClient.sendHennepinDocUploadsEmails(application, List.of(testFile, testFile));
    verify(applicationRepository).updateStatus("someId", UPLOADED_DOC, "Hennepin",
        Status.DELIVERED);

    wireMockServer.verify(2, postToMailgun()
        .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
        .withRequestBodyPart(requestBodyPart("from", senderEmail))
        .withRequestBodyPart(requestBodyPart("to", hennepinEmail))
        .withRequestBodyPart(requestBodyPart("html", emailContent))
        .withRequestBodyPart(aMultipart()
            .withName("subject")
            .withHeader(CONTENT_TYPE, containing(TEXT_PLAIN_VALUE))
            .withBody(containing("Verification docs for Jane Doe"))
            .matchingType(ANY)
            .build())
    );

    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor
        .forClass(Map.class);
    verify(emailContentCreator).createHennepinDocUploadsHTML(captor.capture());
    Map<String, String> actual = captor.getValue();
    assertThat(actual).containsAllEntriesOf(Map.of(
        "name", "Jane Doe",
        "dob", "10/04/2020",
        "last4SSN", "6789",
        "phoneNumber", phoneNumber,
        "email", email));
  }

  @Test
  void sendsHennepinDocUploadsEmailAsLaterDocs() {
    wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));
    var applicationData = new ApplicationData();
    var phoneNumber = "(603) 879-1111";
    var email = "jane@example.com";
    var pagesData = new PagesDataBuilder()
        .withPageData("matchInfo", Map.of(
            "firstName", "Jane",
            "lastName", "Doe",
            "dateOfBirth", List.of("10", "04", "2020"),
            "ssn", "123-45-6789",
            "phoneNumber", phoneNumber,
            "email", email)).build();
    applicationData.setPagesData(pagesData);
    ApplicationFile testFile = new ApplicationFile("testfile".getBytes(), "");
    UploadedDocument doc1 = new UploadedDocument("somefile1", "", "", "", 1000);
    UploadedDocument doc2 = new UploadedDocument("somefile2", "", "", "", 1000);
    applicationData.setUploadedDocs(List.of(doc1, doc2));
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Hennepin)
        .timeToComplete(null)
        .flow(LATER_DOCS)
        .build();
    var emailContent = "content";
    when(emailContentCreator.createHennepinDocUploadsHTML(anyMap())).thenReturn(emailContent);

    mailGunEmailClient.sendHennepinDocUploadsEmails(application, List.of(testFile, testFile));

    wireMockServer.verify(1, postToMailgun()
        .withBasicAuth(credentials)
        .withRequestBodyPart(requestBodyPart("from", senderEmail))
        .withRequestBodyPart(requestBodyPart("to", hennepinEmail))
        .withRequestBodyPart(requestBodyPart("html", emailContent))
        .withRequestBodyPart(requestBodyPart("subject", "Verification docs for Jane Doe"))
    );

    ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
    verify(emailContentCreator).createHennepinDocUploadsHTML(captor.capture());
    Map<String, String> actual = captor.getValue();
    assertThat(actual).containsAllEntriesOf(Map.of(
        "name", "Jane Doe",
        "dob", "10/04/2020",
        "last4SSN", "6789",
        "phoneNumber", phoneNumber,
        "email", email));
  }


  @Test
  void sendsDownloadCafAlertEmail() {
    String emailContent = "content";
    String confirmationId = "confirmation id";
    String ip = "some ip";

    when(emailContentCreator.createDownloadCafAlertContent(confirmationId, ip, ENGLISH))
        .thenReturn(emailContent);

    wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));

    mailGunEmailClient.sendDownloadCafAlertEmail(confirmationId, ip, ENGLISH);

    wireMockServer.verify(postToMailgun()
        .withBasicAuth(credentials)
        .withRequestBody(containing(String.format("from=%s", securityEmail)))
        .withRequestBody(containing(String.format("to=%s", auditEmail)))
        .withRequestBody(containing(String.format("subject=%s", "Caseworker+CAF+downloaded")))
        .withRequestBody(containing(String.format("html=%s", emailContent)))
    );
  }

  @Test
  void sendResubmitEmail() {
    wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));

    var fileContent = "testfile";
    var fileName = "somefile1";
    ApplicationFile testFile = new ApplicationFile(fileContent.getBytes(), fileName);
    var applicationId = "someId";

    Application application = buildApplicationWithUploadedDoc(applicationId, fileName);
    var emailContent = "content";
    when(emailContentCreator.createResubmitEmailContent(UPLOADED_DOC, ENGLISH))
        .thenReturn(emailContent);
    when(pdfGenerator
        .generateForUploadedDocument(any(UploadedDocument.class), anyInt(), any(Application.class),
            any())).thenReturn(testFile);

    mailGunEmailClient.resubmitFailedEmail(hennepinEmail, UPLOADED_DOC, testFile, application);

    verify(emailContentCreator).createResubmitEmailContent(UPLOADED_DOC, ENGLISH);

    var expectedEmailRequest = postToMailgun().withBasicAuth(credentials)
        .withRequestBodyPart(requestBodyPart("from", senderEmail))
        .withRequestBodyPart(requestBodyPart("to", hennepinEmail))
        .withRequestBodyPart(requestBodyPart("html", emailContent))
        .withRequestBodyPart(requestBodyPart("subject",
            "MN Benefits Application " + applicationId + " Resubmission"))
        .withRequestBodyPart(attachment("filename=\"" + fileName + "\"", fileContent));
    wireMockServer.verify(expectedEmailRequest);
  }

  private MultipartValuePattern requestBodyPart(String name, String expectedBody) {
    return aMultipart()
        .withName(name)
        .withHeader(CONTENT_TYPE, containing(TEXT_PLAIN_VALUE))
        .withBody(equalTo(expectedBody))
        .matchingType(ANY)
        .build();
  }

  private MultipartValuePattern attachment(String contentDisposition, String fileContent) {
    return aMultipart("attachment")
        .withHeader(CONTENT_DISPOSITION, containing(contentDisposition))
        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_OCTET_STREAM_VALUE))
        .withBody(equalTo(fileContent))
        .matchingType(ANY)
        .build();
  }

  @NotNull
  private RequestPatternBuilder postToMailgun() {
    return postRequestedFor(urlPathEqualTo("/"));
  }

  private Application buildApplicationWithUploadedDoc(String applicationId, String fileName) {
    var applicationData = new ApplicationData();
    var phoneNumber = "(603) 879-1111";
    var email = "jane@example.com";
    var pagesData = new PagesDataBuilder()
        .withPageData("matchInfo", Map.of(
            "firstName", "Jane",
            "lastName", "Doe",
            "dateOfBirth", List.of("10", "04", "2020"),
            "ssn", "123-45-6789",
            "phoneNumber", phoneNumber,
            "email", email
        ))
        .build();
    applicationData.setPagesData(pagesData);
    UploadedDocument doc1 = new UploadedDocument(fileName, "", "", "", 1000);
    applicationData.setUploadedDocs(List.of(doc1));
    return Application.builder()
        .id(applicationId)
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Hennepin)
        .timeToComplete(null)
        .flow(LATER_DOCS)
        .build();
  }

  @Nested
  @Tag("demo-testing")
  class EmailContentCreatorDemoTest {

    @BeforeEach
    void setUp() {
      emailContentCreator = mock(EmailContentCreator.class);

      WireMockConfiguration options = WireMockConfiguration.wireMockConfig().dynamicPort();
      wireMockServer = new WireMockServer(options);
      wireMockServer.start();
      port = wireMockServer.port();
      WireMock.configureFor(port);
      mailGunEmailClient = new MailGunEmailClient(
          senderEmail,
          securityEmail,
          auditEmail,
          hennepinEmail,
          "http://localhost:" + port,
          mailGunApiKey,
          emailContentCreator,
          false,
          MAX_ATTACHMENT_SIZE,
          "demo",
          applicationRepository,
          messageSource);
    }

    @Test
    void sendEmailToTheApplicantFromDemo() {
      var applicationData = new ApplicationData();
      String recipientEmail = "someRecipient";
      String emailContent = "content";
      SnapExpeditedEligibility snapExpeditedEligibility = ELIGIBLE;
      String confirmationId = "someConfirmationId";
      when(emailContentCreator.createFullClientConfirmationEmail(applicationData,
          confirmationId,
          programs,
          snapExpeditedEligibility,
          ccapExpeditedEligibility,
          ENGLISH)).thenReturn(emailContent);

      wireMockServer.stubFor(post(anyUrl())
          .willReturn(aResponse().withStatus(200)));

      String fileContent = "someContent";
      String fileName = "someFileName";
      mailGunEmailClient.sendConfirmationEmail(applicationData,
          recipientEmail,
          confirmationId,
          List.of(Program.SNAP),
          snapExpeditedEligibility,
          ccapExpeditedEligibility,
          List.of(new ApplicationFile(fileContent.getBytes(), fileName)), ENGLISH);

      wireMockServer.verify(postToMailgun()
          .withBasicAuth(credentials)
          .withRequestBodyPart(requestBodyPart("from", senderEmail))
          .withRequestBodyPart(requestBodyPart("to", recipientEmail))
          .withRequestBodyPart(
              requestBodyPart("subject", "[DEMO] We received your MNbenefits application"))
          .withRequestBodyPart(requestBodyPart("html", emailContent))
          .withRequestBodyPart(
              attachment(String.format("filename=\"%s\"", fileName), fileContent)));
    }
  }
}
