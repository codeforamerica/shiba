package org.codeforamerica.shiba.pages.journeys;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("journey")
public class UserJourneyPageTest extends JourneyTest {
    @Test
    void shouldValidateContactInfoEmailEvenIfEmailNotSelected() {
        completeFlowFromLandingPageThroughContactInfo(List.of(PROGRAM_CCAP));
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", "email.com");
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Contact Info");
    }

    @Test
    void shouldNotShowValidationWarningWhenPressingBackOnFormWithNotEmptyValidationCondition() {
        getToPersonalInfoScreen(List.of(PROGRAM_CCAP));
        testPage.enter("firstName", "defaultFirstName");
        testPage.enter("lastName", "defaultLastName");
        testPage.enter("dateOfBirth", "01/12/1928");
        testPage.clickContinue();
        testPage.goBack();

        assertThat(driver.findElementsByClassName("form-group--error")).hasSize(0);
        assertThat(driver.findElementsByClassName("text--error")).hasSize(0);
    }
}
