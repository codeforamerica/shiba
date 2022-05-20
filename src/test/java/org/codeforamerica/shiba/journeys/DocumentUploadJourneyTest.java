package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.web.multipart.MultipartFile;

@Tag("documentUploadJourney")
public class DocumentUploadJourneyTest extends JourneyTest {

  @Test
  void whenDocumentUploadFailsThenThereShouldBeAnError() throws InterruptedException, IOException {
    getToDocumentUploadScreen();
    uploadXfaFormatPdf();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
        "This PDF is in an old format. Try converting it to an image or uploading a screenshot instead.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
        "0 files added");
    testPage.clickLink("remove");

    uploadPasswordProtectedPdf();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
        "This PDF is password protected. Try removing the password or uploading a screenshot instead.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
        "0 files added");
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains(
        "hidden");
    testPage.clickLink("remove");

    uploadInvalidJpg();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).
        contains("This image cannot be uploaded to your application. Please try another file or upload a screenshot instead.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files"))
        .getText()).contains("0 files added");
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
        .contains("hidden");
    testPage.clickLink("remove");

    doThrow(new InterruptedException())
        .when(documentRepository).upload(any(String.class), any(MultipartFile.class));

    uploadJpgFile();

    List<WebElement> deleteLinks = driver.findElements(By.linkText("delete"));
    assertThat(deleteLinks.size()).isEqualTo(0);
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).isEqualTo(
        "There was an issue processing this file on our end. Sorry about that! Please try another file or upload a screenshot instead.");
  }

  @Test
  void shouldShowErrorsAndWarningsProperly() {
    getToDocumentUploadScreen();
    uploadJpgFile();

    // should disallow adding files with types that are not on the allow list
    driver.executeScript(
        "$('#document-upload').get(0).dropzone.addFile({name: 'testFile.xyz', size: 1000, type: 'not-an-image'})");
    assertThat(driver.findElements(By.className("text--error")).get(0).getText())
        .contains("You can't upload files of this type");
    testPage.clickLink("remove");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText())
        .isEqualTo("1 file added");

    // special case error message for the .heic (iPhone) file type that is not on the allow list
    driver.executeScript(
        "$('#document-upload').get(0).dropzone.addFile({name: 'testFile.heic', size: 1000, type: 'not-an-image'})");
    assertThat(driver.findElements(By.className("text--error")).get(0).getText())
        .contains("HEIC files, an iPhone file type, are not accepted.");
    testPage.clickLink("remove");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText())
        .isEqualTo("1 file added");

    // should show max filesize error message for files that are too big
    long largeFilesize = 21000000L;
    driver.executeScript(
        "$('#document-upload').get(0).dropzone.addFile({name: 'testFile.pdf', size: "
        + largeFilesize + ", type: 'not-an-image'})");
    int maxFileSize = uploadDocumentConfiguration.getMaxFilesize();
    assertThat(driver.findElement(By.className("text--error")).getText()).contains(
        "This file is too large and cannot be uploaded (max size: " + maxFileSize + " MB)");
    testPage.clickLink("remove");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText())
        .isEqualTo("1 file added");



    // should alert the user when they have uploaded the maximum number of files
    IntStream.range(0, 19).forEach(c -> uploadJpgFile());
    assertThat(driver.findElement(By.id("max-files")).getText()).contains(
        "You have uploaded the maximum number of files (20). You will have the opportunity to share more with a caseworker later.");
  }
}
