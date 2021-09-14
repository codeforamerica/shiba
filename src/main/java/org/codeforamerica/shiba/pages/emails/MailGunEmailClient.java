package org.codeforamerica.shiba.pages.emails;

import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
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
  private final String securityEmail;
  private final String auditEmail;
  private final String hennepinEmail;
  private final String mailGunApiKey;
  private final EmailContentCreator emailContentCreator;
  private final boolean shouldCC;
  private final WebClient webClient;
  private final PdfGenerator pdfGenerator;
  private final String activeProfile;
  private final ApplicationRepository applicationRepository;
  private final MessageSource messageSource;

  public MailGunEmailClient(@Value("${sender-email}") String senderEmail,
      @Value("${security-email}") String securityEmail,
      @Value("${audit-email}") String auditEmail,
      @Value("${hennepin-email}") String hennepinEmail,
      @Value("${mail-gun.url}") String mailGunUrl,
      @Value("${mail-gun.api-key}") String mailGunApiKey,
      EmailContentCreator emailContentCreator,
      @Value("${mail-gun.shouldCC}") boolean shouldCC,
      PdfGenerator pdfGenerator,
      @Value("${spring.profiles.active:Unknown}") String activeProfile,
      ApplicationRepository applicationRepository,
      MessageSource messageSource
  ) {
    this.senderEmail = senderEmail;
    this.securityEmail = securityEmail;
    this.auditEmail = auditEmail;
    this.hennepinEmail = hennepinEmail;
    this.mailGunApiKey = mailGunApiKey;
    this.emailContentCreator = emailContentCreator;
    this.shouldCC = shouldCC;
    this.webClient = WebClient.builder().baseUrl(mailGunUrl).build();
    this.pdfGenerator = pdfGenerator;
    this.activeProfile = activeProfile;
    this.applicationRepository = applicationRepository;
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
    var emailBody = emailContentCreator.createShortClientConfirmationEmail(applicationId, locale);
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
        applicationId,
        programs,
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        locale);
    sendEmail(subject, senderEmail, recipientEmail, emailContent, emptyList());
    log.info("Next steps email sent for " + applicationId);
  }

  @Override
  public void sendCaseWorkerEmail(String recipientEmail,
      String recipientName,
      String applicationId,
      ApplicationFile applicationFile) {
    String subject = "MNBenefits.org Application for " + recipientName;
    String emailBody = emailContentCreator.createCaseworkerHTML();
    List<String> emailsToCC = new ArrayList<>();
    if (shouldCC) {
      emailsToCC.add(senderEmail);
    }
    sendEmail(subject, senderEmail, recipientEmail, emailsToCC, emailBody,
        List.of(applicationFile), false);
    log.info("Caseworker email sent for " + applicationFile.getFileName());
  }

  @Override
  public void sendDownloadCafAlertEmail(String confirmationId, String ip, Locale locale) {
    var emailBody = emailContentCreator.createDownloadCafAlertContent(confirmationId, ip, locale);
    sendEmailFromFormData("Caseworker CAF downloaded", securityEmail, auditEmail, emailBody);
    log.info("Download CAF Alert Email sent for " + confirmationId);
  }

  @Override
  public void sendHennepinDocUploadsEmails(Application application) {
    PageData personalInfo = application.getApplicationData().getPageData("personalInfo");
    PageData contactInfo = application.getApplicationData().getPageData("contactInfo");

    if (application.getFlow() == FlowType.LATER_DOCS) {
      personalInfo = application.getApplicationData().getPageData("matchInfo");
      contactInfo = application.getApplicationData().getPageData("matchInfo");
    }

    String fullName = String.join(" ", personalInfo.get("firstName").getValue(0),
        personalInfo.get("lastName").getValue(0));
    String subject = "Verification docs for " + fullName;

    // Generate email content
    String emailBody = emailContentCreator.createHennepinDocUploadsHTML(
        getEmailContentArgsForHennepinDocUploads(personalInfo, contactInfo, fullName));

    // Generate Uploaded Doc PDFs
    List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
    byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument uploadedDocument = uploadedDocs.get(i);
      ApplicationFile fileToSend = pdfGenerator
          .generateForUploadedDocument(uploadedDocument, i, application, coverPage);

      if (fileToSend.getFileBytes().length > 0) {
        log.info("Now attaching: %s original filename: %s".formatted(
            fileToSend.getFileName(),
            uploadedDocument.getFilename()));
        sendEmail(subject, senderEmail, hennepinEmail, emptyList(), emailBody, List.of(fileToSend),
            true);
      }
    }
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, Status.DELIVERED);
  }

  @NotNull
  private HashMap<String, String> getEmailContentArgsForHennepinDocUploads(
      PageData personalInfo,
      PageData contactInfo,
      String fullName) {
    var emailContentArgs = new HashMap<String, String>();
    emailContentArgs.put("name", fullName);
    var dob = personalInfo.get("dateOfBirth");
    if (dob.getValue(0).isBlank()) {
      emailContentArgs.put("dob", "");
    } else {
      emailContentArgs.put("dob", dob.getValue(0) + "/" + dob.getValue(1) + "/" + dob.getValue(2));
    }
    if (personalInfo.get("ssn").getValue(0).isBlank()) {
      emailContentArgs.put("last4SSN", "");
    } else {
      emailContentArgs.put("last4SSN", personalInfo.get("ssn").getValue(0).substring(7));
    }
    emailContentArgs.put("phoneNumber", contactInfo.get("phoneNumber").getValue(0));
    emailContentArgs.put("email", contactInfo.get("email").getValue(0));
    return emailContentArgs;
  }

  @Override
  public void sendLaterDocsConfirmationEmail(String recipientEmail, Locale locale) {
    String subject = emailContentCreator.createClientLaterDocsConfirmationEmailSubject(locale);
    String body = emailContentCreator.createClientLaterDocsConfirmationEmailBody(locale);
    sendEmail(subject, senderEmail, recipientEmail, body, emptyList());
    log.info("later docs confirmation email sent to " + recipientEmail);
  }

  @Override
  public void resubmitFailedEmail(String recipientEmail, Document document,
      ApplicationFile applicationFile, Application application) {
    MDC.put("applicationFile", applicationFile.getFileName());
    String subject = "MN Benefits Application %s Resubmission".formatted(application.getId());
    String body = emailContentCreator.createResubmitEmailContent(document, ENGLISH);
    sendEmail(subject, senderEmail, recipientEmail, body, List.of(applicationFile));
  }

  public void sendEmail(String subject, String senderEmail, String recipientEmail, String emailBody,
      List<ApplicationFile> attachments) {
    sendEmail(subject, senderEmail, recipientEmail, emptyList(), emailBody,
        attachments, false);
  }

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
