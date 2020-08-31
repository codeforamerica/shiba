package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class MailGunEmailClient implements EmailClient {
    private final RestTemplate restTemplate;
    private final String senderEmail;
    private final String mailGunUrl;
    private final String mailGunApiKey;
    private final EmailContentCreator emailContentCreator;

    public MailGunEmailClient(RestTemplate restTemplate,
                              @Value("${sender-email}") String senderEmail,
                              @Value("${mail-gun.url}") String mailGunUrl,
                              @Value("${mail-gun.api-key}") String mailGunApiKey,
                              EmailContentCreator emailContentCreator) {
        this.restTemplate = restTemplate;
        this.senderEmail = senderEmail;
        this.mailGunUrl = mailGunUrl;
        this.mailGunApiKey = mailGunApiKey;
        this.emailContentCreator = emailContentCreator;
    }

    @Override
    public void sendConfirmationEmail(String recipient,
                                      String confirmationId,
                                      ExpeditedEligibility expeditedEligibility) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.put("from", List.of(senderEmail));
        form.put("to", List.of(recipient));
        form.put("subject", List.of("We received your application"));
        form.put("html", List.of(emailContentCreator.createHTML(confirmationId, expeditedEligibility)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBasicAuth("api", mailGunApiKey);

        restTemplate.postForLocation(mailGunUrl, new HttpEntity<>(form, requestHeaders));
    }
}
