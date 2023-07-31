package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.ArrayList;
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
  
  @Test
  void whenLaterDocsUploadFailsThenThereShouldBeAnError() throws InterruptedException, IOException {
	getToLaterDocsUploadScreen();
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
  void shouldShowLaterDocsUploadErrorsAndWarningsProperly() {
	getToLaterDocsUploadScreen();
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
  
  @Test
  void whenHealthcareRenewalUploadFailsThenThereShouldBeAnError() throws InterruptedException, IOException {
	getToHealthcareRenewalUploadScreen();
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
  void shouldShowHealthcareRenewalUploadErrorsAndWarningsProperly() {
	getToHealthcareRenewalUploadScreen();
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
    IntStream.range(0, 49).forEach(c -> uploadJpgFile());
    assertThat(driver.findElement(By.id("max-files")).getText()).contains(
        "You have uploaded the maximum number of files (50). You will have the opportunity to share more with a caseworker later.");
  }
  
	/**
	 * This test verifies: 1.) When multiple files with the same name are uploaded
	 * the name is modified to make the file name unique. 2.) We can delete a
	 * specific file that has been renamed.
	 */
	@Test
	void shouldDisplayUniqueNamesForUploadFilesWithTheSameName() {
		getToLaterDocsUploadScreen();

		// Upload the same file 3 times
		uploadJpgFile(); // shiba+file.jpg
		uploadJpgFile(); // becomes "(1) shiba+file.jpg", we will delete this one later
		uploadPdfFile(); // test-caf.pdf, mix in a file with a different name
		uploadJpgFile(); // becomes "(2) shiba+file.jpg"

		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).isEqualTo("4 files added");

		List<WebElement> webElements = driver.findElements(By.id("file"));
		assert (webElements.size() == 4);
		ArrayList<String> fileNames = new ArrayList<String>();
		WebElement anchor = null;
		for (WebElement webElement : webElements) {
			String fileName = webElement.findElement(By.className("filename-text")).getText();
			// Look for the second file uploaded, we will delete it later.
			if (fileName.compareTo("(1) shiba+file.jpg") == 0) {
				anchor = webElement.findElement(By.tagName("a"));
			}
			fileNames.add(fileName);
		}
		assertThat(fileNames
				.containsAll(List.of("(2) shiba+file.jpg", "(1) shiba+file.jpg", "shiba+file.jpg", "test-caf.pdf")));

		// Delete the second file uploaded.
		anchor.click();
		// Click the confirmation
		driver.findElement(By.id("form-submit-button")).click();
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).isEqualTo("3 files added");

		webElements = driver.findElements(By.id("file"));
		assert (webElements.size() == 3);
		fileNames = new ArrayList<String>();
		for (WebElement webElement : webElements) {
			fileNames.add(webElement.findElement(By.className("filename-text")).getText());
		}
		assertThat(fileNames.containsAll(List.of("(2) shiba+file.jpg", "shiba+file.jpg", "test-caf.pdf")));
	}
}
