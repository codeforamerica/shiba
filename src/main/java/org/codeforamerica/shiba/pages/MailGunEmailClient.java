package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

@Component
public class MailGunEmailClient implements EmailClient {
    private final String senderEmail;
    private final String securityEmail;
    private final String auditEmail;
    private final String supportEmail;
    private final String mailGunApiKey;
    private final EmailContentCreator emailContentCreator;
    private final boolean shouldCC;
    private final WebClient webClient;

    public MailGunEmailClient(@Value("${sender-email}") String senderEmail,
                              @Value("${security-email}") String securityEmail,
                              @Value("${audit-email}") String auditEmail,
                              @Value("${support-email}") String supportEmail,
                              @Value("${mail-gun.url}") String mailGunUrl,
                              @Value("${mail-gun.api-key}") String mailGunApiKey,
                              EmailContentCreator emailContentCreator,
                              @Value("${mail-gun.shouldCC}") boolean shouldCC) {
        this.senderEmail = senderEmail;
        this.securityEmail = securityEmail;
        this.auditEmail = auditEmail;
        this.supportEmail = supportEmail;
        this.mailGunApiKey = mailGunApiKey;
        this.emailContentCreator = emailContentCreator;
        this.shouldCC = shouldCC;
        this.webClient = WebClient.builder().baseUrl(mailGunUrl).build();
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

        MDC.put("confirmationId", confirmationId);

        webClient.post()
                .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
                .body(fromMultipartData(form))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public void sendCaseWorkerEmail(String recipientEmail,
                                    String recipientName,
                                    String confirmationId,
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

        MDC.put("confirmationId", confirmationId);

        webClient.post()
                .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
                .body(fromMultipartData(form))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public void sendDownloadCafAlertEmail(String confirmationId, String ip) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.put("from", List.of(securityEmail));
        form.put("to", List.of(auditEmail));
        form.put("subject", List.of("Caseworker CAF downloaded"));
        form.put("html", List.of(emailContentCreator.createDownloadCafAlertContent(confirmationId, ip)));

        webClient.post()
                .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
                .body(fromFormData(form))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    @Override
    public void sendNonPartnerCountyAlert(String applicationId, ZonedDateTime submissionTime) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.put("from", List.of(senderEmail));
        form.put("to", List.of(supportEmail));
        form.put("subject", List.of("ALERT new non-partner application submitted"));
        form.put("html", List.of(emailContentCreator.createNonCountyPartnerAlert(applicationId, submissionTime)));

        webClient.post()
                .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
                .body(fromFormData(form))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
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
