package org.codeforamerica.shiba.pages.journeys;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.SuccessPage;
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

@Tag("journey")
public class UserJourneyPageTest extends JourneyTest {
    @Test
    void intercomButtonIsPresent() {
        await().atMost(5, TimeUnit.SECONDS).until(() -> !driver.findElementsById("intercom-frame").isEmpty());
        assertThat(driver.findElementById("intercom-frame")).isNotNull();
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlow() {
        nonExpeditedFlowToSuccessPage(true, true, smartyStreetClient, true, true);
        assertThat(driver.findElementsById("healthcareCoverage")).isEmpty();
    }

    @Test
    void userCanCompleteTheNonExpeditedFlowWithNoEmployment() {
        nonExpeditedFlowToSuccessPage(false, false, smartyStreetClient);
        assertThat(driver.findElementsById("healthcareCoverage")).isNotEmpty();
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
    void minimumFlowForSnapOnly() {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_SNAP), smartyStreetClient);
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Finish application now");

        assertThat(testPage.getTitle()).isEqualTo("Additional Info");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
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

    @Test
    void shouldNotShowValidationWarningWhenPressingBackOnFormWithNotEmptyValidationCondition() {
        getToPersonalInfoScreen(List.of(PROGRAM_CCAP));
        testPage.enter("firstName", "defaultFirstName");
        testPage.enter("lastName", "defaultLastName");
        testPage.enter("dateOfBirth", "01/12/1928");
        testPage.clickContinue();
        testPage.goBack();

        assertThat(driver.findElementsByClassName("form-group--error")).hasSize(0);
        assertThat(driver.findElementsByClassName("text--error")).hasSize(0);
    }
}
