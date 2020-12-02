package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.output.caf.ExpeditedEligibility.ELIGIBLE;

@Component
public class EmailContentCreator {
    private final MessageSource messageSource;
    private final String expeditedWaitTime = "email.expedited-wait-time";
    private final String nonExpeditedWaitTime = "email.nonexpedited-wait-time";
    private final String clientBody = "email.client-body";
    private final String caseworkerBody = "email.caseworker-body";
    private final String downloadCafAlert = "email.download-caf-alert";
    private final String nonCountyPartnerAlert = "email.non-county-partner-alert";

    public EmailContentCreator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    String createClientHTML(String confirmationId, ExpeditedEligibility expeditedEligibility) {
        String eligibilitySpecificVerbiage;
        if (ELIGIBLE == expeditedEligibility) {
            eligibilitySpecificVerbiage = getMessage(expeditedWaitTime, null);
        } else {
            eligibilitySpecificVerbiage = getMessage(nonExpeditedWaitTime, null);
        }
        return wrapHtml(getMessage(clientBody, List.of(eligibilitySpecificVerbiage, confirmationId)));
    }

    String createCaseworkerHTML() {
        return wrapHtml(getMessage(caseworkerBody, null));
    }

    String createDownloadCafAlertContent(String confirmationId, String ip) {
        return getMessage(downloadCafAlert, List.of(confirmationId, ip));
    }

    public String createNonCountyPartnerAlert(String confirmationId, ZonedDateTime submissionTime) {
        String formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm").format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
        return getMessage(nonCountyPartnerAlert, List.of(confirmationId, formattedTime));
    }

    private String getMessage(String expeditedWaitTime, @Nullable List<String> args) {
        return messageSource.getMessage(
                expeditedWaitTime,
                ofNullable(args).map(List::toArray).orElse(null),
                LocaleContextHolder.getLocale()
        );
    }

    private String wrapHtml(String message) {
        return String.format("<html><body>%s</body><html>", message);
    }

}
