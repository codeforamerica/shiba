package org.codeforamerica.shiba.pages.features;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

public class DocumentsTest extends FeatureTest {
    @Test
    void shouldSkipDocumentUploadFlowIfNoApplicablePrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        completeFlowFromReviewInfoToDisability(List.of(PROGRAM_CCAP));

        // Recommend proof of job loss (if programs were applicable)
        testPage.enter("hasWorkSituation", YES.getDisplayValue());
        testPage.clickContinue();
        // Recommend proof of income (if programs were applicable)
        testPage.enter("areYouWorking", YES.getDisplayValue());
        testPage.clickButton("Add a job");
        testPage.enter("employersName", "some employer");
        testPage.clickContinue();
        testPage.enter("selfEmployment", YES.getDisplayValue());
        paidByTheHourOrSelectPayPeriod();
        testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        // Recommend proof of shelter (if programs were applicable)
        testPage.enter("homeExpenses", "Rent");
        testPage.clickContinue();

        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        assertThat(driver.getTitle()).isEqualTo("Success");
    }

    @Test
    void shouldSkipDocumentUploadFlowIfNotApplicableRegardlessOfPrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP, PROGRAM_CASH, PROGRAM_EA, PROGRAM_GRH), smartyStreetClient);
        completeFlowFromReviewInfoToDisability(List.of(PROGRAM_SNAP, PROGRAM_CASH, PROGRAM_EA, PROGRAM_GRH));

        // Do not recommend proof of job loss
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        // Do not recommend proof of income
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        // Do not recommend proof of shelter
        testPage.enter("homeExpenses", "None of the above");
        testPage.clickContinue();

        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        assertThat(driver.getTitle()).isEqualTo("Success");
    }

    @Test
    void shouldDisplayDocumentRecommendationsForSingleApplicant() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_GRH, PROGRAM_SNAP), smartyStreetClient);
        completeFlowFromReviewInfoToDisability(List.of(PROGRAM_GRH, PROGRAM_SNAP));

        // Recommend proof of job loss
        testPage.enter("hasWorkSituation", YES.getDisplayValue());
        testPage.clickContinue();
        // Recommend proof of income
        testPage.enter("areYouWorking", YES.getDisplayValue());
        testPage.clickButton("Add a job");
        testPage.enter("employersName", "some employer");
        testPage.clickContinue();
        testPage.enter("selfEmployment", YES.getDisplayValue());
        paidByTheHourOrSelectPayPeriod();
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        // Recommend proof of shelter
        testPage.enter("homeExpenses", "Rent");
        testPage.clickContinue();

        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        assertThat(driver.getTitle()).isEqualTo("Document Recommendation");
        assertThat(driver.findElementsByClassName("success-icons")).hasSize(3);
    }

    @Test
    void shouldSkipDocumentRecommendationsWhenNoEligibleProgram() {
        // Skip because only CCAP
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        completeFlowFromReviewInfoToDisability(List.of(PROGRAM_CCAP));

        testPage.enter("hasWorkSituation", YES.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", YES.getDisplayValue());
        testPage.clickButton("Add a job");
        testPage.enter("employersName", "some employer");
        testPage.clickContinue();
        testPage.enter("selfEmployment", YES.getDisplayValue());
        paidByTheHourOrSelectPayPeriod();
        testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "Rent");
        testPage.clickContinue();

        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        assertThat(driver.getTitle()).isEqualTo("Success");
    }

    @Test
    void shouldSkipDocumentRecommendationsIfChoseEligibleProgramsButNoOnEmploymentStatusNoOnHasWorkSituationAndNoneOfTheAboveOnHomeExpenses() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_GRH, PROGRAM_SNAP), smartyStreetClient);
        completeFlowFromReviewInfoToDisability(List.of(PROGRAM_GRH, PROGRAM_SNAP));

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

        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");
        assertThat(driver.getTitle()).isEqualTo("Success");
    }

    @Test
    void shouldDisplayDocumentRecommendationsForHousehold() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_SNAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();
        testPage.enter("livingSituation", "None of these");
        testPage.clickContinue();
        testPage.enter("goingToSchool", NO.getDisplayValue());
        testPage.enter("isPregnant", NO.getDisplayValue());
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", YES.getDisplayValue());
        testPage.enter("hasDisability", NO.getDisplayValue());

        // Recommend proof of job loss
        testPage.enter("hasWorkSituation", YES.getDisplayValue());
        testPage.clickContinue();
        // Recommend proof of income
        testPage.enter("areYouWorking", YES.getDisplayValue());
        testPage.clickButton("Add a job");
        testPage.enter("whoseJobIsIt", "defaultFirstName defaultLastName");
        testPage.clickContinue();
        testPage.enter("employersName", "some employer");
        testPage.clickContinue();
        testPage.enter("selfEmployment", YES.getDisplayValue());

        testPage.enter("paidByTheHour", YES.getDisplayValue());
        testPage.enter("hourlyWage", "1");
        testPage.clickContinue();
        testPage.enter("hoursAWeek", "30");
        testPage.clickContinue();
        testPage.clickButton("No, that's it.");
        testPage.enter("currentlyLookingForJob", NO.getDisplayValue());

        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.clickContinue();
        // Recommend proof of shelter
        testPage.enter("homeExpenses", "Rent");
        testPage.clickContinue();

        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        assertThat(driver.getTitle()).isEqualTo("Document Recommendation");
        assertThat(driver.findElementsByClassName("success-icons")).hasSize(3);
    }

}
