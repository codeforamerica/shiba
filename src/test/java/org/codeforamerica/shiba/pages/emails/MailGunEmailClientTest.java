package org.codeforamerica.shiba.pages.emails;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MultipartValuePattern.MatchingType.ANY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
class MailGunEmailClientTest {

    MailGunEmailClient mailGunEmailClient;

    EmailContentCreator emailContentCreator;
    @Autowired
    PdfGenerator pdfGenerator;

    WireMockServer wireMockServer;

    int port;

    String mailGunApiKey = "someMailGunApiKey";

    String senderEmail = "someSenderEmail";
    String securityEmail = "someSecurityEmail";
    String auditEmail = "someAuditEmail";
    String supportEmail = "someSupportEmail";
    String hennepinEmail = "someHennepinEmail";

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
                supportEmail,
                hennepinEmail,
                "http://localhost:" + port,
                mailGunApiKey,
                emailContentCreator,
                false,
                pdfGenerator);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void sendsEmailToTheRecipient() {
        String recipientEmail = "someRecipient";
        String emailContent = "content";
        SnapExpeditedEligibility snapExpeditedEligibility = ELIGIBLE;
        String confirmationId = "someConfirmationId";
        when(emailContentCreator.createClientHTML(confirmationId, snapExpeditedEligibility, Locale.ENGLISH)).thenReturn(emailContent);

        wireMockServer.stubFor(post(anyUrl())
                .willReturn(aResponse().withStatus(200)));

        String fileContent = "someContent";
        String fileName = "someFileName";
        mailGunEmailClient.sendConfirmationEmail(
                recipientEmail,
                confirmationId,
                snapExpeditedEligibility,
                List.of(new ApplicationFile(fileContent.getBytes(), fileName)), Locale.ENGLISH);

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/"))
                .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
                .withRequestBodyPart(aMultipart()
                        .withName("from")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(senderEmail))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("to")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(recipientEmail))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("subject")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo("We received your application"))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("html")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(emailContent))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("attachment")
                        .withHeader(HttpHeaders.CONTENT_DISPOSITION, containing(String.format("filename=\"%s\"", fileName)))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .withBody(equalTo(fileContent))
                        .matchingType(ANY)
                        .build()));
    }

    @Test
    void sendsEmailToTheCaseWorker() {
        String recipientEmail = "someRecipient";
        String emailContent = "content";
        String recipientName = "test recipient";
        when(emailContentCreator.createCaseworkerHTML(Locale.ENGLISH)).thenReturn(emailContent);

        wireMockServer.stubFor(post(anyUrl())
                .willReturn(aResponse().withStatus(200)));

        String fileContent = "someContent";
        String fileName = "someFileName";
        mailGunEmailClient.sendCaseWorkerEmail(
                recipientEmail,
                recipientName,
                "appId",
                new ApplicationFile(fileContent.getBytes(), fileName)
        );

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/"))
                .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
                .withRequestBody(notMatching(".*name=\"cc\".*"))
                .withRequestBodyPart(aMultipart()
                        .withName("from")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(senderEmail))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("to")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(recipientEmail))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("subject")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo("MNBenefits.org Application for " + recipientName))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("html")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(emailContent))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("attachment")
                        .withHeader(HttpHeaders.CONTENT_DISPOSITION, containing(String.format("filename=\"%s\"", fileName)))
                        .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .withBody(equalTo(fileContent))
                        .matchingType(ANY)
                        .build()));
    }

    @Test
    void sendsHennepinDocUploadsEmail() {
        wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));
        var applicationData = new ApplicationData();
        var phoneNumber = "(603) 879-1111";
        var email = "jane@example.com";
        var pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("personalInfo", Map.of(
                        "firstName", List.of("Jane"),
                        "lastName", List.of("Doe"),
                        "otherName", List.of(""),
                        "dateOfBirth", List.of("10", "04", "2020"),
                        "ssn", List.of("123-45-6789"),
                        "sex", List.of("FEMALE"),
                        "maritalStatus", List.of("NEVER_MARRIED"),
                        "livedInMnWholeLife", List.of("false"),
                        "moveToMnDate", List.of("11", "03", "2020"),
                        "moveToMnPreviousCity", List.of("")
                )),
                new PageDataBuilder("contactInfo", Map.of(
                        "phoneNumber", List.of(phoneNumber),
                        "email", List.of(email),
                        "phoneOrEmail", List.of("PHONE")
                ))
        ));
        applicationData.setPagesData(pagesData);
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        var emailContent = "content";
        when(emailContentCreator.createHennepinDocUploadsHTML(anyMap())).thenReturn(emailContent);

        mailGunEmailClient.sendHennepinDocUploadsEmail(application);

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/"))
                .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
                .withRequestBodyPart(aMultipart()
                        .withName("from")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(senderEmail))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("to")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(hennepinEmail))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("html")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(emailContent))
                        .matchingType(ANY)
                        .build())
                .withRequestBodyPart(aMultipart()
                        .withName("subject")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(containing("Verification docs for Jane Doe"))
                        .matchingType(ANY)
                        .build())
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

        when(emailContentCreator.createDownloadCafAlertContent(confirmationId, ip, Locale.ENGLISH)).thenReturn(emailContent);

        wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));

        mailGunEmailClient.sendDownloadCafAlertEmail(confirmationId, ip, Locale.ENGLISH);

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/"))
                .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
                .withRequestBody(containing(String.format("from=%s", securityEmail)))
                .withRequestBody(containing(String.format("to=%s", auditEmail)))
                .withRequestBody(containing(String.format("subject=%s", "Caseworker+CAF+downloaded")))
                .withRequestBody(containing(String.format("html=%s", emailContent)))
        );
    }

    @Test
    void shouldCCSenderEmail_whenSendingCaseworkerEmail_ifCCFlagIsTrue() {
        String recipientEmail = "someRecipient";
        String emailContent = "content";
        String recipientName = "test recipient";
        when(emailContentCreator.createCaseworkerHTML(Locale.ENGLISH)).thenReturn(emailContent);

        mailGunEmailClient = new MailGunEmailClient(
                senderEmail,
                "", "", "", "", "http://localhost:" + port,
                mailGunApiKey,
                emailContentCreator,
                true,
                pdfGenerator);

        wireMockServer.stubFor(post(anyUrl())
                .willReturn(aResponse().withStatus(200)));

        String fileContent = "someContent";
        String fileName = "someFileName";
        mailGunEmailClient.sendCaseWorkerEmail(
                recipientEmail,
                recipientName,
                "appId",
                new ApplicationFile(fileContent.getBytes(), fileName)
        );

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/"))
                .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
                .withRequestBodyPart(aMultipart()
                        .withName("cc")
                        .withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.TEXT_PLAIN_VALUE))
                        .withBody(equalTo(senderEmail))
                        .matchingType(ANY)
                        .build()));
    }

    @Test
    void shouldNonPartnerCountyAlert() {
        String emailContent = "content";
        String confirmationId = "confirmation id";
        ZonedDateTime submissionTime = ZonedDateTime.now();
        when(emailContentCreator.createNonCountyPartnerAlert(confirmationId, submissionTime, Locale.ENGLISH)).thenReturn(emailContent);

        wireMockServer.stubFor(post(anyUrl()).willReturn(aResponse().withStatus(200)));

        mailGunEmailClient.sendNonPartnerCountyAlert(confirmationId, submissionTime);

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/"))
                .withBasicAuth(new BasicCredentials("api", mailGunApiKey))
                .withRequestBody(containing(String.format("from=%s", senderEmail)))
                .withRequestBody(containing(String.format("to=%s", supportEmail)))
                .withRequestBody(containing(String.format("subject=%s", "ALERT+new+non-partner+application+submitted")))
                .withRequestBody(containing(String.format("html=%s", emailContent)))
        );
    }
}