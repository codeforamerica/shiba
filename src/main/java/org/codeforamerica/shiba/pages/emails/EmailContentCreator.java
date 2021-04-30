package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;

@Component
public class EmailContentCreator {
    private final MessageSource messageSource;
    private final String snapExpeditedWaitTime = "email.snap-expedited-wait-time";
    private final String snapNonExpeditedWaitTime = "email.snap-nonexpedited-wait-time";
    private final String clientBody = "email.client-body";
    private final String downloadCafAlert = "email.download-caf-alert";
    private final String nonCountyPartnerAlert = "email.non-county-partner-alert";
    private final String laterDocsConfirmationEmailSubject = "later-docs.confirmation-email-subject";
    private final String laterDocsConfirmationEmailBody = "later-docs.confirmation-email-body";
    private final String laterDocsConfirmationEmailLink = "later-docs.confirmation-email-body-link";

    public EmailContentCreator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String createClientHTML(String confirmationId, SnapExpeditedEligibility snapExpeditedEligibility, Locale locale) {
        String eligibilitySpecificVerbiage;
        if (ELIGIBLE == snapExpeditedEligibility) {
            eligibilitySpecificVerbiage = getMessage(snapExpeditedWaitTime, null, locale);
        } else {
            eligibilitySpecificVerbiage = getMessage(snapNonExpeditedWaitTime, null, locale);
        }
        return wrapHtml(getMessage(clientBody, List.of(eligibilitySpecificVerbiage, confirmationId), locale));
    }

    public String createClientLaterDocsConfirmationEmailBody(Locale locale) {
        String clientConfirmationEmailBody = getMessage(laterDocsConfirmationEmailBody, null, locale);
        String clientConfirmationEmailLink = getMessage(laterDocsConfirmationEmailLink, null, locale);

        return wrapHtml(clientConfirmationEmailBody + clientConfirmationEmailLink);
    }

    public String createClientLaterDocsConfirmationEmailSubject(Locale locale) {
        String clientConfirmationEmailMessageSubject = getMessage(laterDocsConfirmationEmailSubject, null, locale);

        return wrapHtml(clientConfirmationEmailMessageSubject);
    }

    public String createCaseworkerHTML(Locale locale) {
        return wrapHtml("<p>This application was submitted on behalf of a client.</p><p>Please keep the file pages in the order they appear in the file; intake workers will be looking for the cover page in front of the CAF.</p>");
    }

    public String createDownloadCafAlertContent(String confirmationId, String ip, Locale locale) {
        return getMessage(downloadCafAlert, List.of(confirmationId, ip), locale);
    }

    public String createNonCountyPartnerAlert(String confirmationId, ZonedDateTime submissionTime, Locale locale) {
        String formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm").format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
        return getMessage(nonCountyPartnerAlert, List.of(confirmationId, formattedTime), locale);
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
        return messageSource.getMessage(
                snapExpeditedWaitTime,
                ofNullable(args).map(List::toArray).orElse(null),
                locale
        );
    }

    private String wrapHtml(String message) {
        return String.format("<html><body>%s</body><html>", message);
    }

}
