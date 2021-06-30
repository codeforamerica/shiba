package org.codeforamerica.shiba.pages.journeys;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Tag("document")
public class DocumentsTest extends JourneyTest {

    @Test
    void shouldSkipDocumentUploadFlowIfNotApplicableRegardlessOfPrograms() {
        List<String> applicantPrograms = List.of(PROGRAM_SNAP, PROGRAM_CASH, PROGRAM_EA, PROGRAM_GRH, PROGRAM_CCAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);

        // Won't recommend proof of job loss
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        // Won't recommend proof of income
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
        // Won't recommend proof of shelter
        testPage.enter("homeExpenses", "None of the above");
        testPage.clickContinue();
        // Won't recommend proof of medical expenses
        navigateTo("medicalExpenses");
        testPage.enter("medicalExpenses", "None of the above");
        testPage.clickContinue();
        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        assertThat(driver.getTitle()).isEqualTo("Success");
    }

    @Test
    void shouldDisplayDocumentRecommendationsForHousehold() {
        // TODO move this test's assertions to DocRecommendationMessageServiceTest
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP), smartyStreetClient);
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        fillOutHousemateInfo(PROGRAM_SNAP);
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.clickContinue();
        testPage.enter("isPreparingMealsTogether", YES.getDisplayValue());
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
        testPage.enter("whoseJobIsIt", "householdMemberFirstName householdMemberLastName");
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

    @Test
    void whenDocumentUploadFailsThenThereShouldBeAnError() throws InterruptedException {
        doThrow(new InterruptedException())
                .when(documentRepositoryService).uploadConcurrently(any(String.class), any(MultipartFile.class));

        getToDocumentUploadScreen();
        uploadJpgFile();

        List<WebElement> deleteLinks = driver.findElements(By.linkText("delete"));
        assertThat(deleteLinks.size()).isEqualTo(0);
        WebElement errorMessage = driver.findElementByClassName("text--error");
        await().until(() -> !errorMessage.getText().isEmpty());
        assertThat(errorMessage.getText()).isEqualTo("Internal Server Error");
    }

    @Test
    void showMaxFileUploadMessageWhenClientHasUploaded20Documents() {
        getToDocumentUploadScreen();
        for (int c = 0; c < 20; c++) {
            uploadJpgFile();
        }

        assertThat(driver.findElementById("max-files").getText()).contains("You have uploaded the maximum number of files (20). You will have the opportunity to share more with a county worker later.");
    }

    @Test
    void showMaxFilesizeErrorMessageWhenClientHasUploadedLargeDocument() {
        getToDocumentUploadScreen();
        long largeFilesize = 21000000L;
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.pdf', size: " + largeFilesize + ", type: 'not-an-image'})");

        int maxFileSize = uploadDocumentConfiguration.getMaxFilesize();
        assertThat(driver.findElementByClassName("text--error").getText()).contains("This file is too large and cannot be uploaded (max size: " + maxFileSize + " MB)");
    }

    @Test
    void showFileTypeErrorMessageWhenClientHasUploadedInvalidFileType() {
        getToDocumentUploadScreen();
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.xyz', size: 1000, type: 'not-an-image'})");

        assertThat(driver.findElementByClassName("text--error").getText()).contains("You can't upload files of this type");
    }

    @Test
    void shouldUpdateFileCountWhenRemoveIsClickedIfAnUploadHasAnError() {
        getToDocumentUploadScreen();
        uploadJpgFile();
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.xyz', size: 1000, type: 'not-an-image'})");
        assertThat(driver.findElementsByClassName("text--error").get(0).getText()).contains("You can't upload files of this type");
        testPage.clickLink("remove");
        assertThat(driver.findElementById("number-of-uploaded-files").getText()).isEqualTo("1 file added");
    }
}
