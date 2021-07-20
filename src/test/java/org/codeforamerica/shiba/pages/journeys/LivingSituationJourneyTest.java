package org.codeforamerica.shiba.pages.journeys;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

@Tag("journey")
public class LivingSituationJourneyTest extends JourneyTest {
    @Test
    void shouldAskLivingSituationIfGRHApplicant() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_GRH), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.getTitle()).isEqualTo("Living situation");
    }

    @Test
    void shouldAskLivingSituationIfGRHApplicantLivingAlone() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_GRH), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", NO.getDisplayValue());
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
        assertThat(driver.getTitle()).isEqualTo("Going to school");
    }
}
