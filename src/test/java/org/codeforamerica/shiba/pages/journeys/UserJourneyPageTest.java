package org.codeforamerica.shiba.pages.journeys;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.Tag;
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
import static org.mockito.Mockito.when;

@Tag("journey")
public class UserJourneyPageTest extends JourneyTest {

    @Test
    void intercomButtonIsPresent() {
        await().atMost(5, TimeUnit.SECONDS).until(() -> !driver.findElementsById("intercom-frame").isEmpty());
        assertThat(driver.findElementById("intercom-frame")).isNotNull();
    }
    
    @Test
    void laterDocsEmailYourCountyFlow() {
        when(featureFlagConfiguration.get("county-hennepin")).thenReturn(FeatureFlag.ON);
        when(featureFlagConfiguration.get("county-morrison")).thenReturn(FeatureFlag.OFF);

    	testPage.clickButton("Upload documents");
        assertThat(driver.getTitle()).isEqualTo("Identify County");

        testPage.enter("county", "Hennepin");
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Match Info");

        testPage.clickLink("< Go Back");
        testPage.enter("county", "Morrison");
        testPage.clickContinue();
        assertThat(driver.getTitle()).isEqualTo("Email Docs To Your County");
    }

    @Test
    void laterDocsSubmissionFlow() {
        when(featureFlagConfiguration.get("county-hennepin")).thenReturn(FeatureFlag.ON);

        testPage.clickButton("Upload documents");
        assertThat(driver.getTitle()).isEqualTo("Identify County");

        testPage.enter("county", "Hennepin");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Match Info");
        testPage.enter("firstName", "defaultFirstName");
        testPage.enter("lastName", "defaultLastName");
        testPage.enter("dateOfBirth", "01/12/1928");
        testPage.enter("ssn", "123456789");
        testPage.enter("caseNumber", "1234567");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("Upload Documents");

        uploadPdfFile();
        await().until(() -> !getAttributeForElementAtIndex(driver.findElementsByClassName("dz-remove"), 0, "innerHTML").isBlank());

        testPage.clickButton("I'm finished uploading");
        assertThat(driver.getTitle()).isEqualTo("Documents Sent");
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlow() {
        nonExpeditedFlowToSuccessPage(true, true, smartyStreetClient, true);
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
            "1, 1, Your county should reach out to you for your interview within 24 hours."
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
            "1, 1, Your county should reach out to you for your interview within 24 hours."
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
