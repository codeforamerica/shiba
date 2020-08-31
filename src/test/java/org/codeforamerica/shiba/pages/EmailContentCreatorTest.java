package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EmailContentCreatorTest {
    private final EmailContentCreator emailContentCreator = new EmailContentCreator();

    @Test
    void includesTheConfirmationNumber() {
        String emailContent = emailContentCreator.createHTML("someNumber", ExpeditedEligibility.UNDETERMINED);

        assertThat(emailContent).contains("someNumber");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ELIGIBLE,Your county will call you in the next 3 days for your phone interview.",
            "NOT_ELIGIBLE,Your county will mail you a notice that will arrive in the next week.",
            "UNDETERMINED,Your county will mail you a notice that will arrive in the next week.",
    })
    void createContentForExpedited(ExpeditedEligibility expeditedEligibility, String expeditedEligibilityContent) {
        String emailContent = emailContentCreator.createHTML("someNumber", expeditedEligibility);

        assertThat(emailContent).contains(expeditedEligibilityContent);
    }
}