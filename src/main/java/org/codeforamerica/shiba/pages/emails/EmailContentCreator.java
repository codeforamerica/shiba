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

import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;

@Component
public class EmailContentCreator {
    private final MessageSource messageSource;
    private final String snapExpeditedWaitTime = "email.snap-expedited-wait-time";
    private final String snapNonExpeditedWaitTime = "email.snap-nonexpedited-wait-time";
    private final String clientBody = "email.client-body";
    private final String caseworkerBody = "email.caseworker-body";
    private final String downloadCafAlert = "email.download-caf-alert";
    private final String nonCountyPartnerAlert = "email.non-county-partner-alert";

    public EmailContentCreator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    String createClientHTML(String confirmationId, SnapExpeditedEligibility snapExpeditedEligibility, Locale locale) {
        String eligibilitySpecificVerbiage;
        if (ELIGIBLE == snapExpeditedEligibility) {
            eligibilitySpecificVerbiage = getMessage(snapExpeditedWaitTime, null, locale);
        } else {
            eligibilitySpecificVerbiage = getMessage(snapNonExpeditedWaitTime, null, locale);
        }
        return wrapHtml(getMessage(clientBody, List.of(eligibilitySpecificVerbiage, confirmationId), locale));
    }

    String createCaseworkerHTML(Locale locale) {
        return wrapHtml(getMessage(caseworkerBody, null, locale));
    }

    String createDownloadCafAlertContent(String confirmationId, String ip, Locale locale) {
        return getMessage(downloadCafAlert, List.of(confirmationId, ip), locale);
    }

    public String createNonCountyPartnerAlert(String confirmationId, ZonedDateTime submissionTime, Locale locale) {
        String formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm").format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
        return getMessage(nonCountyPartnerAlert, List.of(confirmationId, formattedTime), locale);
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
