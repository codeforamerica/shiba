package org.codeforamerica.shiba.pages.journeys;

import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("journey")
public class UserJourneyMockMvcTest extends AbstractShibaMockMvcTest {
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postExpectingSuccess("languagePreferences",
                             Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH"))
        );
    }

    @Test
    void healthcareCoverageDoesNotDisplayOnSuccessPageWhenClientAlreadyHasHealthcare() throws Exception {
        var successPage = nonExpeditedFlowToSuccessPage(true, true, true, true);
        assertThat(successPage.findElementById("healthcareCoverage")).isNull();
    }

    @Test
    void healthcareCoverageDisplaysOnSuccessPageWhenClientDoesNotHaveHealthcare() throws Exception {
        var successPage = nonExpeditedFlowToSuccessPage(false, false, false, false);
        assertThat(successPage.findElementById("healthcareCoverage")).isNotNull();
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlowWithNoEmployment() throws Exception {
        nonExpeditedFlowToSuccessPage(true, false);
    }

    @Test
    void userCanCompleteTheExpeditedFlowWithoutBeingExpedited() throws Exception {
        completeFlowFromLandingPageThroughReviewInfo("SNAP", "CCAP");

 /*
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP, PROGRAM_CCAP), smartyStreetClient);
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Yes, I want to see if I qualify");

        testPage.enter("addHouseholdMembers", NO.getDisplayValue());
        testPage.enter("moneyMadeLast30Days", "123");

        testPage.clickContinue();
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1233");

        testPage.clickContinue();
        testPage.enter("payRentOrMortgage", YES.getDisplayValue());

        testPage.enter("homeExpensesAmount", "333");
        testPage.clickContinue();

        testPage.enter("payForUtilities", "Cooling");
        testPage.clickContinue();

        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());

        assertThat(driver.findElement(By.tagName("p")).getText()).contains("A caseworker will contact you within 5-7 days to review your application.");

        testPage.clickButton("Finish application");
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    */
    }

}
