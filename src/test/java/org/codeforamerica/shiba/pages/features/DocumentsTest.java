package org.codeforamerica.shiba.pages.features;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class DocumentsTest extends FeatureTest {
    @Test
    void shouldSkipDocumentUploadFlowIfNoApplicablePrograms() {
        List<String> applicantPrograms = List.of(PROGRAM_CCAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);

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
        List<String> applicantPrograms = List.of(PROGRAM_SNAP, PROGRAM_CASH, PROGRAM_EA, PROGRAM_GRH);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);

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
        List<String> applicantPrograms = List.of(PROGRAM_GRH, PROGRAM_SNAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);

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
        List<String> applicantPrograms = List.of(PROGRAM_CCAP);
        completeFlowFromLandingPageThroughReviewInfo(applicantPrograms, smartyStreetClient);
        completeFlowFromReviewInfoToDisability(applicantPrograms);

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
        List<String> applicantPrograms = List.of(PROGRAM_GRH, PROGRAM_SNAP);
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

    @Test
    void shouldDisplayImageUploadInformation() {
        getToDocumentUploadScreen();
        uploadDefaultFile();
        await().until(() -> !driver.findElementsByClassName("file-count").get(0).getAttribute("innerHTML").isBlank());
        assertThat(driver.findElementsByClassName("filename-text").get(0).getAttribute("innerHTML").contains("shiba")).isTrue();
        assertThat(driver.findElementsByClassName("filename-text").get(0).getAttribute("innerHTML").contains("jpg")).isTrue();
        assertThat(driver.findElementsByClassName("file-details").get(0).getAttribute("innerHTML").contains("1 image")).isTrue();
        assertThat(driver.findElementsByClassName("file-details").get(0).getAttribute("innerHTML").contains("51.7")).isTrue();
        assertThat(driver.findElementsByClassName("file-details").get(0).getAttribute("innerHTML").contains("KB")).isTrue();
    }

    @Test
    void shouldDisplayDocumentUploadInformation() {
        getToDocumentUploadScreen();
        uploadDefaultDocument();
        assertThat(driver.findElementsByClassName("filename-text").get(0).getAttribute("innerHTML").contains("test-caf")).isTrue();
        assertThat(driver.findElementsByClassName("filename-text").get(0).getAttribute("innerHTML").contains("pdf")).isTrue();
        assertThat(driver.findElementsByClassName("file-details").get(0).getAttribute("innerHTML").contains("0.4")).isTrue();
        assertThat(driver.findElementsByClassName("file-details").get(0).getAttribute("innerHTML").contains("MB")).isTrue();
        assertThat(driver.findElementsByClassName("file-details").get(0).getAttribute("innerHTML").contains("1 image")).isFalse();
    }

    @Test
    void whenDocumentUploadFailsThenThereShouldBeAnError() throws IOException, InterruptedException {
        doThrow(new InterruptedException())
                .when(documentUploadService).upload(any(String.class), any(MultipartFile.class));

        getToDocumentUploadScreen();
        uploadDefaultFile();

        List<WebElement> deleteLinks = driver.findElements(By.linkText("delete"));
        assertThat(deleteLinks.size()).isEqualTo(0);
        WebElement errorMessage = driver.findElementById("file-error-message");
        await().until(() -> !errorMessage.getText().isEmpty());
        assertThat(errorMessage.getText()).isEqualTo("Internal Server Error");
    }

    @Test
    void deletingUploadedFileShouldLoadDocumentUploadScreenUponConfirmDeletion() {
        getToDocumentUploadScreen();
        uploadDefaultFile();
        waitForDocumentUploadToComplete();
        List<WebElement> deleteLinks = driver.findElements(By.linkText("delete"));
        assertThat(deleteLinks.size()).isEqualTo(1);
        testPage.clickLink("delete");

        assertThat(testPage.getTitle()).isEqualTo("Delete a file");
        testPage.clickButton("Yes, delete the file");

        assertThat(testPage.getTitle()).isEqualTo("Upload Documents");
        deleteLinks = driver.findElements(By.linkText("delete"));
        assertThat(deleteLinks.size()).isEqualTo(0);
    }

    @Test
    void deletingUploadedFileWithPlussesShouldPreservePlusses() {
        getToDocumentUploadScreen();
        uploadDefaultFile();
        waitForDocumentUploadToComplete();
        testPage.clickLink("delete");

        assertThat(driver.findElement(By.tagName("h1")).getText()).contains("shiba+file.jpg");
    }

    @Test
    void showMaxFileUploadMessageWhenClientHasUploaded20Documents() {
        getToDocumentUploadScreen();
        for (int c = 0; c < 20; c++) {
            uploadDefaultFile();
        }

        assertThat(driver.findElementById("max-files").getText()).contains("You have uploaded the maximum number of files (20). You will have the opportunity to share more with a county worker later.");
    }

    @Test
    void showMaxFilesizeErrorMessageWhenClientHasUploadedLargeDocument() {
        getToDocumentUploadScreen();
        long largeFilesize = 50000000000L;
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.pdf', size: " + largeFilesize + ", type: 'not-an-image'})");

        int maxFileSize = uploadDocumentConfiguration.getMaxFilesize();
        assertThat(driver.findElementById("file-error-message").getText()).contains("This file is too large and cannot be uploaded (max size: " + maxFileSize + " MB)");
    }

    @Test
    void showFileTypeErrorMessageWhenClientHasUploadedInvalidFileType() {
        getToDocumentUploadScreen();
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.xyz', size: 1000, type: 'not-an-image'})");

        assertThat(driver.findElementById("file-error-message").getText()).contains("You can't upload files of this type");
    }
}
