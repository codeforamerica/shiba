package org.codeforamerica.shiba.pages.journeys;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

@Tag("ccap")
public class CCAPTest extends JourneyTest {
    @Test
    void shouldSkipChildcareAssistancePageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
    }

    @Test
    void shouldSkipJobSearchPageIfCCAPNotSelected() {
        List<String> applicantPrograms = List.of(PROGRAM_SNAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        assertThat(testPage.getTitle()).isEqualTo("Income Up Next");
    }

    @Test
    void shouldSkipRealEstatePageIfCCAPNotSelected() {
        List<String> applicantPrograms = List.of(PROGRAM_SNAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "None of the above");
        testPage.clickContinue();
        testPage.enter("payForUtilities", "None of the above");
        testPage.clickContinue();
        testPage.enter("energyAssistance", NO.getDisplayValue());
        testPage.enter("supportAndCare", NO.getDisplayValue());
        testPage.enter("haveVehicle", NO.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
    }

    @Test
    void shouldSkipRealEstatePageIfCCAPNotSelectedWithHouseholdMember() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
        testPage.enter("goingToSchool", YES.getDisplayValue());
        testPage.enter("isPregnant", NO.getDisplayValue());
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", YES.getDisplayValue());
        testPage.enter("hasDisability", NO.getDisplayValue());
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "None of the above");
        testPage.clickContinue();
        testPage.enter("payForUtilities", "None of the above");
        testPage.clickContinue();
        testPage.enter("energyAssistance", NO.getDisplayValue());
        testPage.enter("supportAndCare", NO.getDisplayValue());
        testPage.enter("haveVehicle", NO.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
    }

    @Test
    void shouldAskRealEstateQuestionIfCCAPNotSelectedByApplicantButHouseholdSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_CCAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("goingToSchool", NO.getDisplayValue());
        testPage.enter("isPregnant", NO.getDisplayValue());
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", YES.getDisplayValue());
        testPage.enter("hasDisability", NO.getDisplayValue());
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "None of the above");
        testPage.clickContinue();
        testPage.enter("payForUtilities", "None of the above");
        testPage.clickContinue();
        testPage.enter("energyAssistance", NO.getDisplayValue());
        testPage.enter("supportAndCare", NO.getDisplayValue());
        testPage.enter("haveVehicle", NO.getDisplayValue());
        testPage.enter("ownRealEstate", NO.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", NO.getDisplayValue());
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
    }

    @Test
    void shouldNotShowMillionDollarQuestionIfNoCCAP() {
        List<String> applicantPrograms = List.of(PROGRAM_SNAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "None of the above");
        testPage.clickContinue();
        testPage.enter("payForUtilities", "None of the above");
        testPage.clickContinue();
        testPage.enter("energyAssistance", NO.getDisplayValue());
        testPage.enter("supportAndCare", NO.getDisplayValue());
        testPage.enter("haveVehicle", NO.getDisplayValue());
        testPage.enter("haveInvestments", YES.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "100");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
    }

    @Test
    void shouldNotShowUnearnedIncomeCcapIfNoOneChoseCcap() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_SNAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
        testPage.enter("goingToSchool", NO.getDisplayValue());
        testPage.enter("isPregnant", NO.getDisplayValue());
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", YES.getDisplayValue());
        testPage.enter("hasDisability", NO.getDisplayValue());

        // Recommend proof of job loss
        testPage.enter("hasWorkSituation", YES.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Future Income");
    }

    @Test
    void shouldSkipWhoIsGoingToSchoolPageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.findElementByClassName("h2").getText()).isEqualTo("Does everyone in your household buy and prepare food with you?");
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.findElementByClassName("h2").getText()).isEqualTo("Is anyone in your household pregnant?");
    }

    @Test
    void shouldAskWhoIsGoingToSchoolAndWhoIsLookingForWorkWhenCCAPIsSelectedInPrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.getTitle()).isEqualTo("Who are the children in need of care?");
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is going to school?");
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is looking for a job");
    }

    @Test
    void shouldAskRelevantCCAPQuestionsWhenCCAPIsSelectedInHouseholdMemberInfo() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_CCAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.getTitle()).isEqualTo("Who are the children in need of care?");
        testPage.enter("whoNeedsChildCare", "defaultFirstName defaultLastName");
        testPage.enter("whoNeedsChildCare", "Me");
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Who are the children that have a parent not living in the home?");
        testPage.enter("whoHasAParentNotLivingAtHome", "defaultFirstName defaultLastName");
        testPage.enter("whoHasAParentNotLivingAtHome", "Me");
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Name of parent outside home");
        List<WebElement> whatAreParentNames = driver.findElementsByName("whatAreTheParentsNames[]");
        whatAreParentNames.get(0).sendKeys("My Parent");
        whatAreParentNames.get(1).sendKeys("Default's Parent");
        testPage.clickContinue();
        testPage.clickButton(YES.getDisplayValue());
        testPage.enter("livingSituation", "None of these");
        testPage.clickContinue();
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is going to school?");
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is looking for a job");
    }

    @Test
    void shouldSkipWhoIsGoingToSchoolAndWhoIsLookingForWorkPageIfCCAPSelectedButAddHouseholdMembersIsFalse() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Pregnant");
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Income Up Next");
    }

    @Test
    void shouldSkipWhoIsLookingForWorkPageIfCCAPIsNotSelectedInHouseholdOrPrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickButton(NO.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Income Up Next");
    }

    @Test
    void shouldShowCCAPInLegalStuffWhenHouseholdSelectsCCAP() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_EA), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_CCAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("legalStuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }

    @Test
    void shouldNotShowCCAPInLegalStuffWhenNotSelectedByAnyone() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_EA), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("legalStuff");
        assertThat(driver.findElements(By.id("ccap-legal"))).isEmpty();
    }
}
