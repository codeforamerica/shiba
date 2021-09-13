package org.codeforamerica.shiba.pages.emails;

import static java.util.stream.Collectors.toList;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

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
    String subject =
        "demo".equals(activeProfile) ? String.format("[DEMO] %s", lms.getMessage("email.subject"))
            : lms.getMessage("email.subject");

    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(recipientEmail));
    form.put("subject", List.of(subject));
    form.put("html", List.of(emailContentCreator.createFullClientConfirmationEmail(
        applicationData,
        applicationId,
        programs,
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        locale)));
    form.put("attachment", applicationFiles.stream().map(this::asResource).collect(toList()));

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromMultipartData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
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

    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    String subject =
        "demo".equals(activeProfile) ? String.format("[DEMO] %s", lms.getMessage("email.subject"))
            : lms.getMessage("email.subject");

    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(recipientEmail));
    form.put("subject", List.of(subject));
    form.put("html", List.of(emailContentCreator.createShortClientConfirmationEmail(
        applicationId,
        locale)));
    form.put("attachment", applicationFiles.stream().map(this::asResource).collect(toList()));

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromMultipartData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
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
    String subject =
        "demo".equals(activeProfile) ? String.format("[DEMO] %s", lms.getMessage("email.subject"))
            : lms.getMessage("email.subject");

    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(recipientEmail));
    form.put("subject", List.of(subject));
    form.put("html", List.of(emailContentCreator.createNextStepsEmail(
        applicationData,
        applicationId,
        programs,
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        locale)));
    form.put("attachment", applicationFiles.stream().map(this::asResource).collect(toList()));

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromMultipartData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
    log.info("Confirmation email sent for " + applicationId);
  }

  @Override
  public void sendCaseWorkerEmail(String recipientEmail,
      String recipientName,
      String applicationId,
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

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromMultipartData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
    log.info("Caseworker email sent for " + applicationFile.getFileName());
  }

  @Override
  public void sendDownloadCafAlertEmail(String confirmationId, String ip, Locale locale) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(securityEmail));
    form.put("to", List.of(auditEmail));
    form.put("subject", List.of("Caseworker CAF downloaded"));
    form.put("html",
        List.of(emailContentCreator.createDownloadCafAlertContent(confirmationId, ip, locale)));

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromFormData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }

  @Override
  public void sendHennepinDocUploadsEmails(Application application) {
    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.put("o:require-tls", List.of("true"));

    // from, to, and sender
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(hennepinEmail));

    PageData personalInfo = application.getApplicationData().getPageData("personalInfo");
    PageData contactInfo = application.getApplicationData().getPageData("contactInfo");

    if (application.getFlow() == FlowType.LATER_DOCS) {
      personalInfo = application.getApplicationData().getPageData("matchInfo");
      contactInfo = application.getApplicationData().getPageData("matchInfo");
    }

    String fullName = String.join(" ", personalInfo.get("firstName").getValue(0),
        personalInfo.get("lastName").getValue(0));
    form.put("subject", List.of("Verification docs for " + fullName));

    // Generate email content
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
    List<Object> emailContent = List
        .of(emailContentCreator.createHennepinDocUploadsHTML(emailContentArgs));
    form.put("html", emailContent);

    // Generate Uploaded Doc PDFs
    List<UploadedDocument> uploadedDocs = application.getApplicationData().getUploadedDocs();
    byte[] coverPage = pdfGenerator.generate(application, UPLOADED_DOC, CASEWORKER).getFileBytes();
    for (int i = 0; i < uploadedDocs.size(); i++) {
      UploadedDocument uploadedDocument = uploadedDocs.get(i);
      ApplicationFile fileToSend = pdfGenerator
          .generateForUploadedDocument(uploadedDocument, i, application, coverPage);

      if (fileToSend.getFileBytes().length > 0) {
        log.info(
            "Now attaching: " + fileToSend.getFileName() + " original filename: " + uploadedDocument
                .getFilename());

        form.put("attachment", List.of(asResource(fileToSend)));

        webClient.post()
            .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
            .body(fromMultipartData(form))
            .retrieve()
            .bodyToMono(Void.class)
            .block();
      }
    }
    applicationRepository.updateStatus(application.getId(), UPLOADED_DOC, Status.DELIVERED);
  }

  @Override
  public void sendLaterDocsConfirmationEmail(String recipientEmail, Locale locale) {
    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(recipientEmail));
    form.put("subject",
        List.of(emailContentCreator.createClientLaterDocsConfirmationEmailSubject(locale)));
    form.put("html",
        List.of(emailContentCreator.createClientLaterDocsConfirmationEmailBody(locale)));

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromMultipartData(form))
        .retrieve()
        .bodyToMono(Void.class)
        .block();
  }

  @Override
  public void resubmitFailedEmail(String recipientEmail, Document document,
      ApplicationFile applicationFile, Application application) {
    MDC.put("applicationFile", applicationFile.getFileName());
    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.put("from", List.of(senderEmail));
    form.put("to", List.of(recipientEmail));
    form.put("subject",
        List.of("MN Benefits Application %s Resubmission".formatted(application.getId())));
    form.put("html",
        List.of(emailContentCreator.createResubmitEmailContent(document, Locale.ENGLISH)));
    form.put("attachment", List.of(asResource(applicationFile)));

    webClient.post()
        .headers(httpHeaders -> httpHeaders.setBasicAuth("api", mailGunApiKey))
        .body(fromMultipartData(form))
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
