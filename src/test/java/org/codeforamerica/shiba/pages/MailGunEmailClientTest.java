package org.codeforamerica.shiba.pages;

import com.sun.xml.messaging.saaj.util.Base64;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.codeforamerica.shiba.output.caf.ExpeditedEligibility.ELIGIBLE;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "mail-gun.url=https://someMailGunUrl",
        "mail-gun.api-key=someMailGunApiKey",
        "sender-email=someSenderEmail"
})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class MailGunEmailClientTest {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MailGunEmailClient mailGunEmailClient;

    @MockBean
    EmailContentCreator emailContentCreator;

    @Value("${mail-gun.url}")
    String mailGunUrl;

    @Value("${mail-gun.api-key}")
    String mailGunApiKey;

    @Value("${sender-email}")
    String senderEmail;

    MockRestServiceServer mockRestServiceServer;

    @BeforeEach
    void setUp() {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate)
                .build();
    }

    @Test
    void sendsEmailToTheRecipient() {
        String encodedBasicAuthCredentials = new String(Base64.encode(String.format("api:%s", mailGunApiKey).getBytes()));

        String recipientEmail = "someRecipient";
        String emailContent = "content";
        LinkedMultiValueMap<String, String> expectedFormData = new LinkedMultiValueMap<>();
        expectedFormData.put("from", List.of(senderEmail));
        expectedFormData.put("to", List.of(recipientEmail));
        expectedFormData.put("subject", List.of("We received your application"));
        expectedFormData.put("html", List.of(emailContent));

        ExpeditedEligibility expeditedEligibility = ELIGIBLE;
        String confirmationId = "someConfirmationId";
        when(emailContentCreator.createHTML(confirmationId, expeditedEligibility)).thenReturn(emailContent);

        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo(mailGunUrl))
                .andExpect(MockRestRequestMatchers.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedBasicAuthCredentials))
                .andExpect(MockRestRequestMatchers.content().formData(expectedFormData))
                .andRespond(MockRestResponseCreators.withSuccess());

        mailGunEmailClient.sendConfirmationEmail(recipientEmail, confirmationId, expeditedEligibility);

        mockRestServiceServer.verify();
    }
}