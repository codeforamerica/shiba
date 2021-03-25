package org.codeforamerica.shiba.pages.features;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

public class MillionDollarQuestionTest extends FeatureTest {
    @Test
    void shouldAskLivingSituationIfCCAPApplicant() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Living situation");
    }

    @Test
    void shouldAskLivingSituationIfGRHApplicant() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_GRH), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Living situation");
    }

    @Test
    void shouldAskLivingSituationIfCCAPHouseholdMember() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_EA), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_CCAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Living situation");
    }

    @Test
    void shouldNotAskLivingSituationIfNotCCAPorGRH() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_EA), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        navigateTo("unearnedIncome");
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Future Income");
    }

}
