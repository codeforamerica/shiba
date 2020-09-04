package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    public void sendConfirmationEmail(String recipientEmail,
                                      String confirmationId,
                                      ExpeditedEligibility expeditedEligibility,
                                      ApplicationFile applicationFile) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.put("from", List.of(senderEmail));
        form.put("to", List.of(recipientEmail));
        form.put("subject", List.of("We received your application"));
        form.put("html", List.of(emailContentCreator.createHTML(confirmationId, expeditedEligibility)));
        form.put("attachment", List.of(asFileSystemResource(applicationFile)));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBasicAuth("api", mailGunApiKey);

        restTemplate.postForLocation(mailGunUrl, new HttpEntity<>(form, requestHeaders));
    }

    @NotNull
    private FileSystemResource asFileSystemResource(ApplicationFile applicationFile) {
        File file = new File(applicationFile.getFileName());
        file.deleteOnExit();
        try {
            Files.write(file.toPath(), applicationFile.getFileBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileSystemResource(file);
    }
}
