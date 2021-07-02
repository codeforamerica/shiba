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
