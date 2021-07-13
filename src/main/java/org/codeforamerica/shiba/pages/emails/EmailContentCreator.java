package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService.DocumentRecommendation;
import org.codeforamerica.shiba.pages.SuccessMessageService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class EmailContentCreator {
    private final MessageSource messageSource;
    private final static String CLIENT_BODY = "email.client-body";
    private final static String CLIENT_WARNING = "email.warning";
    private final static String CLIENT_DOC_RECS = "email.doc-recs";
    private final static String DOWNLOAD_CAF_ALERT = "email.download-caf-alert";
    private final static String NON_COUNTY_PARTNER_ALERT = "email.non-county-partner-alert";
    private final static String LATER_DOCS_CONFIRMATION_EMAIL_SUBJECT = "later-docs.confirmation-email-subject";
    private final static String LATER_DOCS_CONFIRMATION_EMAIL_BODY = "later-docs.confirmation-email-body";
    private final static String LATER_DOCS_CONFIRMATION_EMAIL_LINK = "later-docs.confirmation-email-body-link";
    private final static String RESUBMIT_EMAIL_BODY="email.resubmit-email";
    private final static String DEMO_PURPOSES_ONLY = "email.demo-purposes-only";
    private final static String SHARE_FEEDBACK = "email.share-feedback";
    private final String activeProfile;
    private final SuccessMessageService successMessageService;
    private final DocRecommendationMessageService docRecommendationMessageService;

    public EmailContentCreator(MessageSource messageSource,
                               @Value("${spring.profiles.active:Unknown}") String activeProfile,
                               SuccessMessageService successMessageService,
                               DocRecommendationMessageService docRecommendationMessageService)
    {
        this.messageSource = messageSource;
        this.activeProfile = activeProfile;
        this.successMessageService = successMessageService;
        this.docRecommendationMessageService = docRecommendationMessageService;
    }

    public String createClientHTML(ApplicationData applicationData, String confirmationId, List<String> programs, SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility, Locale locale) {
        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
        String successMessage = successMessageService.getSuccessMessage(programs, snapExpeditedEligibility, ccapExpeditedEligibility, locale);
        String content = lms.getMessage(CLIENT_BODY, List.of(confirmationId, successMessage));
        String warning = lms.getMessage(CLIENT_WARNING);

        List<DocumentRecommendation> documentRecommendations = docRecommendationMessageService.getConfirmationEmailDocumentRecommendations(applicationData, locale);

        if (documentRecommendations.size() > 0) {
            final StringBuilder builder = new StringBuilder();
            documentRecommendations.forEach(docRec -> {
                String listElement = "<li>" + docRec.title + ": " + docRec.explanation + "</li>";
                builder.append(listElement);
            });
            content = "%s<p>%s</p>".formatted(content, lms.getMessage(CLIENT_DOC_RECS, List.of(builder.toString())));
        }

        if ("demo".equals(activeProfile)) {
            content = "%s<p>%s</p><p>%s</p>".formatted(content, lms.getMessage(DEMO_PURPOSES_ONLY), lms.getMessage(SHARE_FEEDBACK));
        }

        return wrapHtml(content + warning);
    }

    public String createClientLaterDocsConfirmationEmailBody(Locale locale) {
        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
        String clientConfirmationEmailBody = lms.getMessage(LATER_DOCS_CONFIRMATION_EMAIL_BODY);
        String clientConfirmationEmailLink = lms.getMessage(LATER_DOCS_CONFIRMATION_EMAIL_LINK);

        return wrapHtml("<p>%s</p><p>%s</p>".formatted(clientConfirmationEmailBody, clientConfirmationEmailLink));
    }

    public String createClientLaterDocsConfirmationEmailSubject(Locale locale) {
        return getMessage(LATER_DOCS_CONFIRMATION_EMAIL_SUBJECT, null, locale);
    }

    public String createCaseworkerHTML() {
        return wrapHtml("<p>This application was submitted on behalf of a client.</p><p>Please keep the file pages in the order they appear in the file; intake workers will be looking for the cover page in front of the CAF.</p>");
    }

    public String createDownloadCafAlertContent(String confirmationId, String ip, Locale locale) {
        return getMessage(DOWNLOAD_CAF_ALERT, List.of(confirmationId, ip), locale);
    }

    public String createNonCountyPartnerAlert(String confirmationId, ZonedDateTime submissionTime, Locale locale) {
        String formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm").format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
        return getMessage(NON_COUNTY_PARTNER_ALERT, List.of(confirmationId, formattedTime), locale);
    }

    public String createResubmitEmailContent(Document document, Locale locale){
        String documentSending = "";
        switch(document) {
            case CAF:
                documentSending = "CAF application";
                break;
            case CCAP:
                documentSending = "CCAP application";
                break;
            case UPLOADED_DOC:
                documentSending = "uploaded document";
                break;
        }
        return wrapHtml(getMessage(RESUBMIT_EMAIL_BODY, List.of(documentSending),locale));
    }

    public String createHennepinDocUploadsHTML(Map<String, String> args) {
        return wrapHtml("<p>These are documents that a client uploaded to MNbenefits.org.</p>" +
                "<p><b>Name:</b> " + args.get("name") + "</p>" +
                "<p><b>DOB:</b> " + args.get("dob") + "</p>" +
                "<p><b>Last 4 digits of SSN:</b> " + args.get("last4SSN") + "</p>" +
                "<p><b>Phone Number:</b> " + args.get("phoneNumber") + "</p>" +
                "<p><b>E-mail:</b> " + args.get("email") + "</p>" +
                "<p>Fields that are blank were not shared by the client in their application.</p>" +
                "<p>Please reach out to help@mnbenefits.org for support.</p>");
    }

    private String getMessage(String snapExpeditedWaitTime, @Nullable List<String> args, Locale locale) {
        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
        return lms.getMessage(snapExpeditedWaitTime, args);
    }

    private String wrapHtml(String message) {
        return "<html><body>%s</body></html>".formatted(message);
    }
}
