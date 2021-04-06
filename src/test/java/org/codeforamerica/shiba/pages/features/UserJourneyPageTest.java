package org.codeforamerica.shiba.pages.features;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

public class UserJourneyPageTest extends FeatureTest {

    @Test
    void intercomButtonIsPresent() {
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        assertThat(driver.findElementById("intercom-frame")).isNotNull();
    }

    @Test
    void percySnapshotTest() {
        // TODO is this a good approach for testing?
        // Landing page
        percy.snapshot(driver.getTitle());

        // Prepare to Apply
        testPage.clickButton("Apply now");
        percy.snapshot(driver.getTitle());

        // Language Preferences
        testPage.clickContinue();
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", "Yes");
        percy.snapshot(driver.getTitle());

        // Choose programs
        testPage.clickContinue();
        List<String> programSelections = List.of(PROGRAM_CCAP, PROGRAM_GRH, PROGRAM_SNAP);
        programSelections.forEach(program -> testPage.enter("programs", program));
        percy.snapshot(driver.getTitle());

        // Intro: Basic Info
        testPage.clickContinue();
        percy.snapshot(driver.getTitle());

        // Personal Info
        testPage.clickContinue();
        fillOutPersonalInfo();
        percy.snapshot(driver.getTitle());

        // Contact Info
        testPage.clickContinue();
        fillOutContactInfo();
        percy.snapshot(driver.getTitle());

        // Home Address
        testPage.clickContinue();
        fillOutAddress();
        percy.snapshot(driver.getTitle());

        // Address Validation
        testPage.clickContinue();
        percy.snapshot(driver.getTitle());

        // Review Info
        testPage.clickButton("Use this address");
        percy.snapshot(driver.getTitle());

        // Do you want to add household members?
        testPage.clickButton("This looks correct");
        percy.snapshot(driver.getTitle());

        // Start Household
        testPage.clickButton("Yes");
        percy.snapshot(driver.getTitle());

        // Housemate: Personal Info
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        percy.snapshot(driver.getTitle());

        // Household members
        testPage.clickContinue();
        percy.snapshot(driver.getTitle());

        // Who are the children in need of care?
        testPage.clickButton("Yes, that's everyone");
        testPage.enter("whoNeedsChildCare", "Me");
        testPage.enter("whoNeedsChildCare", "defaultFirstName defaultLastName");
        percy.snapshot(driver.getTitle());

        // Who are the children that have a parent not living in the home?
        testPage.clickContinue();
        testPage.enter("whoHasAParentNotLivingAtHome", "Me");
        percy.snapshot(driver.getTitle());

        // Name of parent outside home
        testPage.clickContinue();
        testPage.enter("whatAreTheParentsNames", "Myparent");
        percy.snapshot(driver.getTitle());

        //warning-page.delete-household-member-warning
        //intro-personal-details.title
        //preparing-meals-together.title
        //going-to-school.title
        //who-is-going-to-school.title
        //pregnant.title
        //who-is-pregnant.title
        //us-citizen.title
        //who-is-non-citizen.title
        //do-you-need-help-immediately.title
        //thirty-day-income.title-household
        //thirty-day-job-income.title
        //liquid-assets-prompt.title-household
        //expedited-expenses.title-one-person
        //expedited-expenses-amount.title-one-person
        //utility-payments.title-one-person
        //energy-assistance.title
        //energy-assistance-more-than-20.title
        //support-and-care.title
        //expedited-migrant-farm-worker.title-one-person
        //legal-stuff.title
        //sign-this-application.title
        //document-recommendation.title
        //upload-documents.title
        //upload-documents-delete-warning.title
        //success.title
        //disability.title
        //work-situation.title
        //intro-income.title
        //employment-status.title
        //income-by-job.title
        //household-selection-for-income.title
        //income-up-next.title
        //ccap-job-search.title
        //who-is-looking-for-job.title
        //unearned-income.title
        //unearned-income-sources.title
        //unearned-income.title
        //unearned-income-sources.title
        //living-situation.title
        //future-income.title
        //employers-name.title
        //self-employment.title
        //paid-by-the-hour.title
        //hourly-wage.title
        //hours-a-week.title
        //pay-period.title
        //income-per-pay-period.title
        //job-builder.title
        //warning-page.go-back-title
        //warning-page.no-data-title
        //start-expenses.title
        //home-expenses.title
        //home-expenses-amount.title
        //savings.title
        //investments.title
        //vehicle.title
        //real-estate.title
        //million-dollar.title
        //sold-assets.title
        //submitting-application.title
        //address-validation.title
        //address-validation.title
        //register-to-vote.title
        //helper.title
        //authorized-rep.title
        //speak-to-county.title
        //spend-on-your-behalf.title
        //helper-contact-info.title
        //additional-info.title
        //error-404.title
        //error-500.title
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
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP, PROGRAM_CCAP), smartyStreetClient);
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
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP, PROGRAM_CCAP), smartyStreetClient);
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
    void shouldHandleDeletionOfLastHouseholdMember() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_EA);
        testPage.clickContinue();
        testPage.clickButtonLink("delete");
        testPage.clickButton("Yes, remove them");
        testPage.goBack();

        assertThat(driver.getTitle()).isEqualTo("Review info");
    }

    @Test
    void shouldValidateContactInfoEmailEvenIfEmailNotSelected() {
        completeFlowFromLandingPageThroughContactInfo(List.of(PROGRAM_CCAP));
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", "email.com");
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Contact Info");
    }

}
