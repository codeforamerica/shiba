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
    void verifyFlowWhenApplicantSelectedCCAPAndHouseholdMemberDidNot() {
        // Applicant selected CCAP for themselves and did not choose CCAP for household member
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
        testPage.enter("goingToSchool", YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is going to school?");
        testPage.clickContinue();
        testPage.enter("isPregnant", NO.getDisplayValue());
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", YES.getDisplayValue());
        testPage.enter("hasDisability", NO.getDisplayValue());
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Job Search");
        testPage.enter("currentlyLookingForJob", YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is looking for a job");
        fillUnearnedIncomeToLegalStuffCCAP();
    }

    @Test
    void verifyFlowWhenOnlyHouseholdMemberSelectedCCAP() {
        // Applicant did not choose CCAP for themselves and selected CCAP for a household member
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_CCAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.getTitle()).isEqualTo("Who are the children in need of care?");

        // Should jump to preparing meals question
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Preparing meals together");
        testPage.goBack();

        testPage.enter("whoNeedsChildCare", "householdMemberFirstName householdMemberLastName");
        testPage.enter("whoNeedsChildCare", "Me");
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Who are the children that have a parent not living in the home?");
        testPage.enter("whoHasAParentNotLivingAtHome", "None of the children have parents living outside the home");

        // Should jump to preparing meals question
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Preparing meals together");
        testPage.goBack();

        testPage.enter("whoHasAParentNotLivingAtHome", "householdMemberFirstName householdMemberLastName");
        testPage.enter("whoHasAParentNotLivingAtHome", "Me");
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Name of parent outside home");

        List<WebElement> whatAreParentNames = driver.findElementsByName("whatAreTheParentsNames[]");
        whatAreParentNames.get(0).sendKeys("My Parent");
        whatAreParentNames.get(1).sendKeys("Default's Parent");

        testPage.clickContinue();
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
        testPage.enter("livingSituation", "None of these");
        testPage.clickContinue();
        testPage.enter("goingToSchool", YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is going to school?");
        testPage.clickContinue();
        testPage.enter("isPregnant", NO.getDisplayValue());
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", YES.getDisplayValue());
        testPage.enter("hasDisability", NO.getDisplayValue());
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Job Search");
        testPage.enter("currentlyLookingForJob", YES.getDisplayValue());
        assertThat(driver.getTitle()).isEqualTo("Who is looking for a job");
        fillUnearnedIncomeToLegalStuffCCAP();
    }

    @Test
    void verifyFlowWhenLiveAloneApplicantHasNotSelectedCCAP() {
        List<String> applicantPrograms = List.of(PROGRAM_SNAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("goingToSchool", YES.getDisplayValue());
        testPage.enter("isPregnant", NO.getDisplayValue());
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", YES.getDisplayValue());
        testPage.enter("hasDisability", NO.getDisplayValue());
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        assertThat(testPage.getTitle()).isEqualTo("Income Up Next");
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        fillFutureIncomeToHaveVehicle();
        assertThat(testPage.getTitle()).isEqualTo("Investments");
        testPage.enter("haveInvestments", YES.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
        navigateTo("legalStuff");
        assertThat(driver.findElements(By.id("ccap-legal"))).isEmpty();
    }

    @Test
    void verifyFlowWhenNoOneHasSelectedCCAPInHousehold() {
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
        assertThat(driver.getTitle()).isEqualTo("Income Up Next");
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Unearned Income");
        testPage.enter("unearnedIncome", "None of the above");
        fillFutureIncomeToHaveVehicle();
        assertThat(testPage.getTitle()).isEqualTo("Investments");
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");

        navigateTo("legalStuff");
        assertThat(driver.findElements(By.id("ccap-legal"))).isEmpty();
    }

    private void fillFutureIncomeToHaveVehicle() {
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Future Income");
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "None of the above");
        testPage.clickContinue();
        testPage.enter("payForUtilities", "None of the above");
        testPage.clickContinue();
        testPage.enter("energyAssistance", NO.getDisplayValue());
        testPage.enter("medicalExpenses", "None of the above");
        testPage.clickContinue();
        testPage.enter("supportAndCare", NO.getDisplayValue());
        testPage.enter("haveVehicle", NO.getDisplayValue());
    }

    private void fillUnearnedIncomeToLegalStuffCCAP() {
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Unearned Income"); // CCAP
        testPage.enter("unearnedIncomeCcap", "None of the above");
        fillFutureIncomeToHaveVehicle();
        assertThat(testPage.getTitle()).isEqualTo("Real Estate");
        testPage.enter("ownRealEstate", NO.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", NO.getDisplayValue());
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
        testPage.goBack();
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("$1M assets");
        testPage.enter("haveMillionDollars", NO.getDisplayValue());
        navigateTo("legalStuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }
}
