package org.codeforamerica.shiba.pages;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserJourneyPageTest extends AbstractBasePageTest {
    private static final String PROGRAM_SNAP = "Food (SNAP)";
    private static final String PROGRAM_CASH = "Cash programs";
    private static final String PROGRAM_GRH = "Housing Support (GRH)";
    private static final String PROGRAM_CCAP = "Child Care Assistance";
    private static final String PROGRAM_EA = "Emergency Assistance";

    @MockBean
    Clock clock;

    @MockBean
    PageEventPublisher pageEventPublisher;

    @MockBean
    SmartyStreetClient smartyStreetClient;

    @MockBean
    MailGunEmailClient mailGunEmailClient;

    @MockBean
    FeatureFlagConfiguration featureFlagConfiguration;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl);
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(smartyStreetClient.validateAddress(any())).thenReturn(Optional.empty());

        when(featureFlagConfiguration.get("document-upload-feature")).thenReturn(FeatureFlag.ON);
        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("send-non-partner-county-alert")).thenReturn(FeatureFlag.OFF);
    }

    @Test
    void intercomButtonIsPresent() {
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        assertThat(driver.findElementById("intercom-frame")).isNotNull();
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlow() {
        nonExpeditedFlowToSuccessPage(true, true, smartyStreetClient);
    }

    @Test
    void userCanCompleteTheNonExpeditedFlowWithNoEmployment() {
        nonExpeditedFlowToSuccessPage(false, false, smartyStreetClient);
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlowWithNoEmployment() {
        nonExpeditedFlowToSuccessPage(true, false, smartyStreetClient);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "123, 1233, A caseworker will contact you within 5-7 days to review your application.",
            "1, 1, A caseworker will contact you within 3 days to review your application."
    })
    void userCanCompleteTheExpeditedFlow(String moneyMadeLast30Days, String liquidAssets, String expeditedServiceDetermination) {
        completeFlowFromLandingPageThroughReviewInfo(List.of("SNAP", "Child Care Assistance"), smartyStreetClient);
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Yes, I want to see if I qualify");

        testPage.enter("addHouseholdMembers", NO.getDisplayValue());
        testPage.enter("moneyMadeLast30Days", moneyMadeLast30Days);

        testPage.clickContinue();
        testPage.enter("haveSavings", YES.getDisplayValue());

        testPage.enter("liquidAssets", liquidAssets);

        testPage.clickContinue();
        testPage.enter("payRentOrMortgage", YES.getDisplayValue());

        testPage.enter("homeExpensesAmount", "333");
        testPage.clickContinue();

        testPage.enter("payForUtilities", "Cooling");
        testPage.clickContinue();

        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());

        assertThat(driver.findElement(By.tagName("p")).getText()).contains(expeditedServiceDetermination);

        testPage.clickButton("Finish application");
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "123, 1233, A caseworker will contact you within 5-7 days to review your application.",
            "1, 1, A caseworker will contact you within 3 days to review your application."
    })
    void userCanCompleteTheExpeditedFlowWithHousehold(String moneyMadeLast30Days, String liquidAssets, String expeditedServiceDetermination) {
        completeFlowFromLandingPageThroughReviewInfo(List.of("SNAP", "Child Care Assistance"), smartyStreetClient);
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Yes, I want to see if I qualify");

        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.enter("moneyMadeLast30Days", moneyMadeLast30Days);

        testPage.clickContinue();
        testPage.enter("haveSavings", YES.getDisplayValue());

        testPage.enter("liquidAssets", liquidAssets);

        testPage.clickContinue();
        testPage.enter("payRentOrMortgage", YES.getDisplayValue());

        testPage.enter("homeExpensesAmount", "333");
        testPage.clickContinue();

        testPage.enter("payForUtilities", "Cooling");
        testPage.clickContinue();

        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());

        assertThat(driver.findElement(By.tagName("p")).getText()).contains(expeditedServiceDetermination);

        testPage.clickButton("Finish application");
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }

    @Test
    void partialFlow() throws IOException {
        getToDocumentUploadScreen();
        completeDocumentUploadFlow();

        SuccessPage successPage = new SuccessPage(driver);
        successPage.downloadPdfs();
        await().until(() -> {
            File[] listFiles = path.toFile().listFiles();
            return Arrays.stream(listFiles).anyMatch(file -> file.getName().contains("_MNB_") && file.getName().endsWith(".pdf"));
        });

        File pdfFile = Arrays.stream(path.toFile().listFiles()).findFirst().orElseThrow();
        PDAcroForm acroForm = PDDocument.load(pdfFile).getDocumentCatalog().getAcroForm();
        assertThat(acroForm.getField("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE").getValueAsString())
                .isEqualTo("ENGLISH");
        assertThat(acroForm.getField("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE").getValueAsString())
                .isEqualTo("ENGLISH");
        assertThat(acroForm.getField("NEED_INTERPRETER").getValueAsString())
                .isEqualTo("Yes");
    }

    @Test
    void deletingUploadedFileShouldLoadDocumentUploadScreenUponConfirmDeletion() {
        getToDocumentUploadScreen();
        uploadDefaultFile();
        uploadDefaultFile();

        testPage.clickLink("delete");

        assertThat(testPage.getTitle()).isEqualTo("Delete a file");
        testPage.clickButton("Yes, delete the file");

        assertThat(testPage.getTitle()).isEqualTo("Upload Documents");
    }

    @Test
    void shouldSkipChildcareAssistancePageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
    }

    @Test
    void shouldSkipJobSearchPageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        completeFlowFromReviewInfoToDisability();
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        assertThat(testPage.getTitle()).isEqualTo("Income Up Next");
    }

    @Test
    void shouldSkipRealEstatePageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        completeFlowFromReviewInfoToDisability();
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
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
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
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Child Care Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
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
        testPage.enter("livingSituation", "I prefer not to say");
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
    void shouldAskMillionDollarQuestionIfRealEstateAnswerIsYes() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Child Care Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();
        testPage.enter("isPreparingMealsTogether", NO.getDisplayValue());
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
        testPage.enter("unearnedIncomeCcap", "Contract for Deed");
        testPage.clickContinue();
        testPage.enter("contractForDeedAmount", "200");
        testPage.clickContinue();
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
        testPage.enter("haveVehicle", YES.getDisplayValue());
        testPage.enter("ownRealEstate", YES.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", NO.getDisplayValue());
        testPage.enter("haveMillionDollars", NO.getDisplayValue());
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
    }

    @Test
    void shouldSkipMillionDollarPageIfNoVehicleInvestmentsRealEstateOrSavings() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"), smartyStreetClient);
        completeFlowFromReviewInfoToDisability();
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
    void shouldShowMillionDollarPageIfYesOnAnAssetPageButNoOnRealEstate() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"), smartyStreetClient);
        completeFlowFromReviewInfoToDisability();
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
        testPage.enter("haveInvestments", YES.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "100");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("$1M assets");
    }

    @Test
    void shouldNotShowMillionDollarQuestionIfNoCCAP() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        completeFlowFromReviewInfoToDisability();
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
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
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
    void shouldSkipDocumentUploadFlowIfNoApplicablePrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        completeFlowFromReviewInfoToDisability();

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
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
        completeFlowFromReviewInfoToDisability();

        // Do not recommend proof of job loss
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        // Do not recommend proof of income
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
        completeFlowFromReviewInfoToDisability();

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
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
        completeFlowFromReviewInfoToDisability();

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
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
        completeFlowFromReviewInfoToDisability();

        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
        testPage.enter("livingSituation", "Paying for my own housing with rent, lease, or mortgage payments");
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
    void shouldSkipWhoIsGoingToSchoolPageIfCCAPNotSelected() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.findElementByClassName("h2").getText()).isEqualTo("Does everyone in your household buy and prepare food with you?");
        testPage.clickButton(NO.getDisplayValue());
        testPage.clickButton(YES.getDisplayValue());
        assertThat(driver.findElementByClassName("h2").getText()).isEqualTo("Is anyone in your household pregnant?");
    }

    @Test
    void shouldAskWhoIsGoingToSchoolAndWhoIsLookingForWorkWhenCCAPIsSelectedInPrograms() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        assertThat(driver.getTitle()).isEqualTo("Who are the children in need of care?");
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
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Child Care Assistance");
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
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", NO.getDisplayValue());
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
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
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
    void shouldHandleDeletionOfLastHouseholdMember() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Food (SNAP)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButtonLink("delete");
        testPage.clickButton("Yes, remove them");
        testPage.goBack();

        assertThat(driver.getTitle()).isEqualTo("Review info");
    }

    @Test
    void shouldValidateContactInfoEmailEvenIfEmailNotSelected() {
        completeFlowFromLandingPageThroughContactInfo(List.of("Child Care Assistance"));
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", "email.com");
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Contact Info");
    }

    @Test
    void shouldShowCCAPInLegalStuffWhenHousholdSelectsCCAP() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Emergency Assistance"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Child Care Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("legalStuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
    }

    @Test
    void shouldNotShowCCAPInLegalStuffWhenNotSelectedByAnyone() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Emergency Assistance"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("legalStuff");
        assertThat(driver.findElements(By.id("ccap-legal"))).isEmpty();
    }

    @Test
    void shouldAskLivingSituationIfCCAPApplicant() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Child Care Assistance"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("unearnedIncome");
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Living situation");
    }

    @Test
    void shouldAskLivingSituationIfGRHApplicant() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Housing Support (GRH)"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("unearnedIncome");
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Living situation");
    }

    @Test
    void shouldAskLivingSituationIfCCAPHouseholdMember() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Emergency Assistance"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Child Care Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");

        navigateTo("unearnedIncome");
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "None of the above");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Living situation");
    }

    @Test
    void shouldNotAskLivingSituationIfNotCCAPorGRH() {
        completeFlowFromLandingPageThroughReviewInfo(List.of("Emergency Assistance"), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo("Emergency Assistance");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        navigateTo("unearnedIncome");
        testPage.enter("unearnedIncome", "None of the above");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Future Income");
    }


}
