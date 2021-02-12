package org.codeforamerica.shiba.pages.emails;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MultipartValuePattern.MatchingType.ANY;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class MailGunEmailClientTest {

    MailGunEmailClient mailGunEmailClient;

    EmailContentCreator emailContentCreator = mock(EmailContentCreator.class);

    WireMockServer wireMockServer;

    int port;

    String mailGunApiKey = "someMailGunApiKey";

    String senderEmail = "someSenderEmail";
    String securityEmail = "someSecurityEmail";
    String auditEmail = "someAuditEmail";
    String supportEmail = "someSupportEmail";

    @BeforeEach
    void setUp() {
        WireMockConfiguration options = WireMockConfiguration.wireMockConfig()
                .dynamicPort();
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
        port = wireMockServer.port();
        WireMock.configureFor(port);
        mailGunEmailClient = new MailGunEmailClient(
                senderEmail,
                securityEmail,
                auditEmail,
                supportEmail,
                "http://localhost:" + port,
                mailGunApiKey,
                emailContentCreator,
                false);
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
                "", "", "", "http://localhost:" + port,
                mailGunApiKey,
                emailContentCreator,
                true);

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