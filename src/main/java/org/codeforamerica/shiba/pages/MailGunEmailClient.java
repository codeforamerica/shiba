package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.util.InMemoryResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class MailGunEmailClient implements EmailClient {
    private final RestTemplate restTemplate;
    private final String senderEmail;
    private final String securityEmail;
    private final String auditEmail;
    private final String mailGunUrl;
    private final String mailGunApiKey;
    private final EmailContentCreator emailContentCreator;
    private final boolean shouldCC;

    public MailGunEmailClient(RestTemplate restTemplate,
                              @Value("${sender-email}") String senderEmail,
                              @Value("${security-email}") String securityEmail,
                              @Value("${audit-email}") String auditEmail,
                              @Value("${mail-gun.url}") String mailGunUrl,
                              @Value("${mail-gun.api-key}") String mailGunApiKey,
                              EmailContentCreator emailContentCreator,
                              @Value("${mail-gun.shouldCC}") boolean shouldCC) {
        this.restTemplate = restTemplate;
        this.senderEmail = senderEmail;
        this.securityEmail = securityEmail;
        this.auditEmail = auditEmail;
        this.mailGunUrl = mailGunUrl;
        this.mailGunApiKey = mailGunApiKey;
        this.emailContentCreator = emailContentCreator;
        this.shouldCC = shouldCC;
    }

    @Override
    public void sendConfirmationEmail(String recipientEmail,
                                      String confirmationId,
                                      ExpeditedEligibility expeditedEligibility,
                                      ApplicationFile applicationFile) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.put("from", List.of(senderEmail));
        form.put("to", List.of(recipientEmail));
        form.put("subject", List.of("We received your application"));
        form.put("html", List.of(emailContentCreator.createClientHTML(confirmationId, expeditedEligibility)));
        form.put("attachment", List.of(asResource(applicationFile)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBasicAuth("api", mailGunApiKey);

        restTemplate.postForLocation(mailGunUrl, new HttpEntity<>(form, requestHeaders));
    }

    @Override
    public void sendCaseWorkerEmail(String recipientEmail,
                                    String recipientName,
                                    ApplicationFile applicationFile) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.put("from", List.of(senderEmail));
        form.put("to", List.of(recipientEmail));
        if (shouldCC) {
            form.put("cc", List.of(senderEmail));
        }
        form.put("subject", List.of("MNBenefits.org Application for " + recipientName));
        form.put("html", List.of(emailContentCreator.createCaseworkerHTML()));
        form.put("attachment", List.of(asResource(applicationFile)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBasicAuth("api", mailGunApiKey);

        restTemplate.postForLocation(mailGunUrl, new HttpEntity<>(form, requestHeaders));
    }

    @Override
    public void sendDownloadCafAlertEmail(String confirmationId, String ip) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.put("from", List.of(securityEmail));
        form.put("to", List.of(auditEmail));
        form.put("subject", List.of("Caseworker CAF downloaded"));
        form.put("html", List.of(emailContentCreator.createDownloadCafAlertContent(confirmationId, ip)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBasicAuth("api", mailGunApiKey);

        restTemplate.postForLocation(mailGunUrl, new HttpEntity<>(form, requestHeaders));
    }

    @NotNull
    private Resource asResource(ApplicationFile applicationFile) {
        return new InMemoryResource(applicationFile.getFileBytes()) {
            @Override
            public String getFilename() {
                return applicationFile.getFileName();
            }
        };
    }
}
