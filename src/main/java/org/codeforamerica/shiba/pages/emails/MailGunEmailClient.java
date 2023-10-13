package org.codeforamerica.shiba.pages.emails;

import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.text.StringEscapeUtils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.security.util.InMemoryResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class MailGunEmailClient implements EmailClient {

  private final String senderEmail;
  private final String mailGunApiKey;
  private final EmailContentCreator emailContentCreator;
  private final WebClient webClient;
  private final String activeProfile;
  private final MessageSource messageSource;

  public MailGunEmailClient(@Value("${sender-email}") String senderEmail,
      @Value("${mail-gun.url}") String mailGunUrl,
      @Value("${mail-gun.api-key}") String mailGunApiKey,
      EmailContentCreator emailContentCreator,
      @Value("${spring.profiles.active:Unknown}") String activeProfile,
      MessageSource messageSource
  ) {
    this.senderEmail = senderEmail;
    this.mailGunApiKey = mailGunApiKey;
    this.emailContentCreator = emailContentCreator;
    this.webClient = WebClient.builder().baseUrl(mailGunUrl).build();
    this.activeProfile = activeProfile;
    this.messageSource = messageSource;
  }

  @Override
  public void sendConfirmationEmail(ApplicationData applicationData,
      String recipientEmail,
      String applicationId,
      List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      List<ApplicationFile> applicationFiles,
      Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    String subject = getEmailSubject("email.subject", lms);
    String emailBody = emailContentCreator.createFullClientConfirmationEmail(
        applicationData,
        applicationId,
        programs,
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        locale);
    sendEmail(subject, senderEmail, recipientEmail, emailBody, applicationFiles);
    log.info("Confirmation email sent for " + applicationId);
  }

  @Override
  public void sendShortConfirmationEmail(ApplicationData applicationData,
      String recipientEmail,
      String applicationId,
      List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      List<ApplicationFile> applicationFiles,
      Locale locale) {
    var lms = new LocaleSpecificMessageSource(locale, messageSource);
    var subject = getEmailSubject("email.subject", lms);
    var emailBody = emailContentCreator.createShortClientConfirmationEmail(applicationData, applicationId, locale);
    sendEmail(subject, senderEmail, recipientEmail, emailBody, applicationFiles);
    log.info("Short confirmation email sent for " + applicationId);
  }

  @Override
  public void sendNextStepsEmail(ApplicationData applicationData,
      String recipientEmail,
      String applicationId,
      List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      List<ApplicationFile> applicationFiles,
      Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    String subject = getEmailSubject("email.next-steps-subject", lms);
    String emailContent = emailContentCreator.createNextStepsEmail(
        programs,
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        locale,
        applicationId);
    sendEmail(subject, senderEmail, recipientEmail, emailContent, emptyList());
    log.info("Next steps email sent for " + applicationId);
  }


  @Override
  public void sendLaterDocsConfirmationEmail(Application application, String confirmationId, String recipientEmail, Locale locale) {
    String subject = emailContentCreator.createClientLaterDocsConfirmationEmailSubject(locale);
    String body = emailContentCreator.createClientLaterDocsConfirmationEmailBody(application.getApplicationData(), confirmationId, locale);
    sendEmail(subject, senderEmail, recipientEmail, body, emptyList());
    log.info(StringEscapeUtils.escapeJava("later docs confirmation email sent for confirmationId " + confirmationId));
  }
  
  @Override
  public void sendHealthcareRenewalConfirmationEmail(Application application, String confirmationId, String recipientEmail, Locale locale) {
    String subject = emailContentCreator.createClientHealthcareRenewalConfirmationEmailSubject(locale);
    String body = emailContentCreator.createClientHealthcareRenewalConfirmationEmailBody(application.getApplicationData(), confirmationId, locale);
    sendEmail(subject, senderEmail, recipientEmail, body, emptyList());
    log.info(StringEscapeUtils.escapeJava("health care renewal confirmation email sent for confirmationId " + confirmationId));
  }

  @Override
  public void resubmitFailedEmail(String recipientEmail, Document document,
      ApplicationFile applicationFile, Application application) {
    MDC.put("applicationFile", applicationFile.getFileName());
    String subject = "MN Benefits Application %s Resubmission".formatted(application.getId());
    String body = emailContentCreator.createResubmitEmailContent(document, ENGLISH);
    sendEmail(subject, senderEmail, recipientEmail, emptyList(), body, List.of(applicationFile),
        false);
  }

  @Override
  public void sendEmail(String subject, String senderEmail, String recipientEmail,
      String emailBody) {
    sendEmail(subject, senderEmail, recipientEmail, emptyList(), emailBody, emptyList(), false);
  }

  @Override
  public void sendEmail(String subject, String senderEmail, String recipientEmail, String emailBody,
      List<ApplicationFile> attachments) {
    sendEmail(subject, senderEmail, recipientEmail, emptyList(), emailBody,
        attachments, false);
  }

  @Override
  public void sendEmail(
      String subject,
      String senderEmail,
      String recipientEmail,
      List<String> emailsToCC,
      String emailBody,
      List<ApplicationFile> attachments,
      boolean requireTls) {
    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(recipientEmail));
    form.put("subject", List.of(subject));
    form.put("html", List.of(emailBody));
    if (!attachments.isEmpty()) {
      form.put("attachment", attachments.stream().map(this::asResource).collect(toList()));
    }
    if (!emailsToCC.isEmpty()) {
      form.put("cc", new ArrayList<>(emailsToCC)); // have to create new list of type List<Object>
    }
    if (requireTls) {
      form.put("o:require-tls", List.of("true"));
    }

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromMultipartData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }

  private void sendEmailFromFormData(
      String subject,
      String senderEmail,
      String recipientEmail,
      String emailBody) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(recipientEmail));
    form.put("subject", List.of(subject));
    form.put("html", List.of(emailBody));
    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromFormData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }

  @NotNull
  private String getEmailSubject(String messageKey, LocaleSpecificMessageSource lms) {
    String subject = lms.getMessage(messageKey);
    if ("demo".equals(activeProfile)) {
      subject = "[DEMO] " + subject;
    }
    return subject;
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
