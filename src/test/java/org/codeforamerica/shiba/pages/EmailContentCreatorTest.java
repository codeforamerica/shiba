package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailContentCreatorTest {
    private final EmailContentCreator emailContentCreator = new EmailContentCreator();

    @Test
    void includesTheConfirmationNumber() {
        String emailContent = emailContentCreator.createClientHTML("someNumber", ExpeditedEligibility.UNDETERMINED);

        assertThat(emailContent).contains("someNumber");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ELIGIBLE,Your county will call you in the next 3 days for your phone interview.",
            "NOT_ELIGIBLE,Your county will mail you a notice that will arrive in the next week.",
            "UNDETERMINED,Your county will mail you a notice that will arrive in the next week.",
    })
    void createContentForExpedited(ExpeditedEligibility expeditedEligibility, String expeditedEligibilityContent) {
        String emailContent = emailContentCreator.createClientHTML("someNumber", expeditedEligibility);

        assertThat(emailContent).contains(expeditedEligibilityContent);
    }

    @Test
    void shouldIncludeConfirmationIdAndIpWhenSendingDownloadAlert() {
        String confirmationId = "confirmation ID";
        String ip = "123.123.123.123";

        String content = emailContentCreator.createDownloadCafAlertContent(confirmationId, ip);

        assertThat(content).isEqualTo(String.format("The CAF with confirmation number %s was downloaded from IP address %s.", confirmationId, ip));
    }

    @Test
    void shouldCreateNonCountyPartnerAlertEmail() {
        String confirmationId = "confirm Id";
        ZonedDateTime submissionTime = ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 11, 10), ZoneOffset.UTC);
        String nonCountyPartnerAlertEmailContent = emailContentCreator.createNonCountyPartnerAlert(confirmationId, submissionTime);

        assertThat(nonCountyPartnerAlertEmailContent).isEqualTo(
                "Application confirm Id was submitted at 01/01/2020 05:10."
        );
    }
}