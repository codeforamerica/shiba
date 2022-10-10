package org.codeforamerica.shiba.pages.emails;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.codeforamerica.shiba.RoutingDestinationMessageService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService.DocumentRecommendation;
import org.codeforamerica.shiba.pages.NextStepsContentService;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.NextStepsContentService.NextStepSection;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class EmailContentCreator {

  private static final ZoneId CENTRAL_TIMEZONE = ZoneId.of("America/Chicago");
  private final static String CLIENT_BODY = "email.client-body";
  private final static String ADDITIONAL_SUPPORT = "email.you-may-be-able-to-receive-more-support";
  private final static String IF_YOU_WANT_AN_UPDATE = "email.if-you-want-an-update-call-your-county";
  private final static String CONFIRMATION_EMAIL_DOC_RECS = "email.confirmation-email-doc-recs";
  private final static String DOCUMENT_RECOMMENDATION_EMAIL = "email.document-recommendation-email";
  private final static String DOWNLOAD_CAF_ALERT = "email.download-caf-alert";
  private final static String NON_COUNTY_PARTNER_ALERT = "email.non-county-partner-alert";
  private final static String LATER_DOCS_CONFIRMATION_EMAIL_SUBJECT = "later-docs.confirmation-email-subject";
  private final static String LATER_DOCS_CONFIRMATION_EMAIL_BODY = "later-docs.confirmation-email-body";
  private final static String LATER_DOCS_CONFIRMATION_EMAIL_LINK = "later-docs.confirmation-email-body-link";
  private final static String RESUBMIT_EMAIL_BODY = "email.resubmit-email";
  private final static String DEMO_PURPOSES_ONLY = "email.demo-purposes-only";
  private final static String SHARE_FEEDBACK = "email.share-feedback";
  private final MessageSource messageSource;
  private final String activeProfile;
  private final NextStepsContentService nextStepsContentService;
  private final DocRecommendationMessageService docRecommendationMessageService;
  private final ApplicationRepository applicationRepository;
  private final RoutingDecisionService routingDecisionService;
  private final RoutingDestinationMessageService routingDestinationMessageService;

  public EmailContentCreator(MessageSource messageSource,
      @Value("${spring.profiles.active:Unknown}") String activeProfile,
      NextStepsContentService nextStepsContentService,
      DocRecommendationMessageService docRecommendationMessageService,
      ApplicationRepository applicationRepository,
      RoutingDecisionService routingDecisionService,
      RoutingDestinationMessageService routingDestinationMessageService) {
    this.messageSource = messageSource;
    this.activeProfile = activeProfile;
    this.nextStepsContentService = nextStepsContentService;
    this.docRecommendationMessageService = docRecommendationMessageService;
    this.applicationRepository = applicationRepository;
    this.routingDecisionService = routingDecisionService;
    this.routingDestinationMessageService = routingDestinationMessageService;
  }

  public String createFullClientConfirmationEmail(ApplicationData applicationData,
      String confirmationId,
      List<String> programs, SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility, Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    
    Application application = applicationRepository.find(applicationData.getId());
    ZonedDateTime submissionTime = application.getCompletedAt().withZoneSameInstant(CENTRAL_TIMEZONE);
    String formattedTime = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            .format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
    
    // Get all routing destinations for this application
    Set<RoutingDestination> routingDestinations = new LinkedHashSet<>();
    DocumentListParser.parse(applicationData).forEach(doc -> {
      List<RoutingDestination> routingDestinationsForThisDoc =
          routingDecisionService.getRoutingDestinations(applicationData, doc);
      routingDestinations.addAll(routingDestinationsForThisDoc);
    });
    
    // Generate human-readable list of routing destinations for success page
    String finalDestinationList = routingDestinationMessageService.generatePhrase(locale,
        application.getCounty(),
        true,
        new ArrayList<>(routingDestinations));
    
    String nextSteps = nextStepsContentService
        .getNextSteps(programs, snapExpeditedEligibility, ccapExpeditedEligibility, locale).stream()
        .map(NextStepSection::message)
        .collect(Collectors.joining("<br><br>"));

    var additionalSupport = lms.getMessage(ADDITIONAL_SUPPORT);
    
    String content = lms.getMessage(CLIENT_BODY,
        List.of(confirmationId, "<br><br>" + nextSteps, additionalSupport, finalDestinationList, formattedTime));

    String docRecs = getDocumentRecommendations(applicationData, locale, lms,
        CONFIRMATION_EMAIL_DOC_RECS);

    if (docRecs.length() > 0) {
      content = "%s<p>%s</p>".formatted(content, docRecs);
    }

    content = addDemoMessage(content, lms);
    return wrapHtml(content);
  }

  public String createDocRecommendationEmail(ApplicationData applicationData) {
    Locale locale = applicationData.getLocale();
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    String content;
    content = getDocumentRecommendations(applicationData, locale, lms,
        DOCUMENT_RECOMMENDATION_EMAIL);
    content = addDemoMessage(content, lms);
    return wrapHtml(content);
  }

  private String getDocumentRecommendations(ApplicationData applicationData, Locale locale,
      LocaleSpecificMessageSource lms, String documentRecommendationMessageKey) {
    List<DocumentRecommendation> documentRecommendations = docRecommendationMessageService
        .getConfirmationEmailDocumentRecommendations(applicationData, locale);
    if (documentRecommendations.size() > 0) {
      final StringBuilder builder = new StringBuilder();
      documentRecommendations.forEach(docRec -> {
        String listElement =
            "<li><strong>" + docRec.title + ":</strong> " + docRec.explanation + "</li>";
        builder.append(listElement);
      });
      return lms.getMessage(documentRecommendationMessageKey, List.of(builder.toString()));
    }
    return "";
  }

  public String createShortClientConfirmationEmail(ApplicationData applicationData, String confirmationId, Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    
    Application application = applicationRepository.find(applicationData.getId());
    ZonedDateTime submissionTime = application.getCompletedAt().withZoneSameInstant(CENTRAL_TIMEZONE);
    String formattedTime = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            .format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
    
    // Get all routing destinations for this application
    Set<RoutingDestination> routingDestinations = new LinkedHashSet<>();
    DocumentListParser.parse(applicationData).forEach(doc -> {
      List<RoutingDestination> routingDestinationsForThisDoc =
          routingDecisionService.getRoutingDestinations(applicationData, doc);
      routingDestinations.addAll(routingDestinationsForThisDoc);
    });
    
    // Generate human-readable list of routing destinations for success page
    String finalDestinationList = routingDestinationMessageService.generatePhrase(locale,
        application.getCounty(),
        true,
        new ArrayList<>(routingDestinations));

    String content = lms.getMessage(CLIENT_BODY,
        List.of(confirmationId, "", lms.getMessage(IF_YOU_WANT_AN_UPDATE), finalDestinationList, formattedTime));

    content = addDemoMessage(content, lms);
    return wrapHtml(content);
  }

  public String createNextStepsEmail(List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility, Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    var sections = nextStepsContentService.getNextSteps(programs, snapExpeditedEligibility,
        ccapExpeditedEligibility, locale);

    sections.add(new NextStepSection("", lms.getMessage(ADDITIONAL_SUPPORT),
        lms.getMessage("email.you-may-be-able-to-receive-more-support-header")));

    String content = sections.stream()
        .map(nextStepSection -> "<strong>" + nextStepSection.title() + ":</strong><br>"
            + nextStepSection.message())
        .collect(Collectors.joining("<br><br>"));

    content = addDemoMessage(content, lms);

    return wrapHtml(content);
  }

  public String createClientLaterDocsConfirmationEmailBody(Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    String clientConfirmationEmailBody = lms.getMessage(LATER_DOCS_CONFIRMATION_EMAIL_BODY);
    String clientConfirmationEmailLink = lms.getMessage(LATER_DOCS_CONFIRMATION_EMAIL_LINK);

    return wrapHtml(
        "<p>%s</p><p>%s</p>".formatted(clientConfirmationEmailBody, clientConfirmationEmailLink));
  }

  public String createClientLaterDocsConfirmationEmailSubject(Locale locale) {
    return getMessage(LATER_DOCS_CONFIRMATION_EMAIL_SUBJECT, null, locale);
  }

  public String createCaseworkerHTML() {
    return wrapHtml(
        "<p>This application was submitted on behalf of a client.</p><p>Please keep the file pages in the order they appear in the file; intake workers will be looking for the cover page in front of the CAF.</p>");
  }

  public String createDownloadCafAlertContent(String confirmationId, String ip, Locale locale) {
    return getMessage(DOWNLOAD_CAF_ALERT, List.of(confirmationId, ip), locale);
  }

  public String createNonCountyPartnerAlert(String confirmationId, ZonedDateTime submissionTime,
      Locale locale) {
    String formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm")
        .format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
    return getMessage(NON_COUNTY_PARTNER_ALERT, List.of(confirmationId, formattedTime), locale);
  }

  public String createResubmitEmailContent(Document document, Locale locale) {
    String documentType = switch (document) {
      case CAF -> "a CAF application.";
      case CCAP -> "a CCAP application.";
      case CERTAIN_POPS -> "a Certain Populations application.";
      case UPLOADED_DOC -> "an uploaded document.";
      default -> throw new IllegalStateException("Unexpected value: " + document);
    };
    String messageBody = getMessage(RESUBMIT_EMAIL_BODY, List.of(documentType), locale);
    return wrapHtml("<p>" + messageBody + "</p>");
  }

  public String createHennepinDocUploadsHTML(Map<String, String> args) {
    return wrapHtml("<p>These are documents that a client uploaded to MNbenefits.mn.gov.</p>" +
        "<p><b>Name:</b> " + args.get("name") + "</p>" +
        "<p><b>DOB:</b> " + args.get("dob") + "</p>" +
        "<p><b>Last 4 digits of SSN:</b> " + args.get("last4SSN") + "</p>" +
        "<p><b>Phone Number:</b> " + args.get("phoneNumber") + "</p>" +
        "<p><b>E-mail:</b> " + args.get("email") + "</p>" +
        "<p>Fields that are blank were not shared by the client in their application.</p>" +
        "<p>Please reach out to help@mnbenefits.org for support.</p>");
  }

  private String addDemoMessage(String content, LocaleSpecificMessageSource lms) {
    if ("demo".equals(activeProfile)) {
      content = "%s<p>%s</p><p>%s</p>"
          .formatted(content, lms.getMessage(DEMO_PURPOSES_ONLY), lms.getMessage(SHARE_FEEDBACK));
    }
    return content;
  }

  private String getMessage(String snapExpeditedWaitTime, @Nullable List<String> args,
      Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    return lms.getMessage(snapExpeditedWaitTime, args);
  }

  private String wrapHtml(String message) {
    return "<html><body>%s</body></html>".formatted(message);
  }
}
