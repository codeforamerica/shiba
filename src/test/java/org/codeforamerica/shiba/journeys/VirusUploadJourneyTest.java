package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Tag("virusUploadJourney")
public class VirusUploadJourneyTest extends JourneyTest {

	@Test
	void whenDocumentUploadVirusThereShouldBeAnError() throws InterruptedException, IOException {
		getToDocumentUploadScreen();

		uploadJpgFile();
		uploadVirusFile("eicar-adobe-acrobat-attachment.pdf");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("1 file added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("disabled");

		uploadVirusFile("eicar-adobe-acrobat-attachment.pdf");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElements(By.className("text--error")).get(1).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("1 file added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("disabled");
		testPage.clickLink("remove");
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("disabled");
		testPage.clickLink("remove");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).doesNotContain("disabled");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).doesNotContain("hidden");
		testPage.clickLink("delete");
		testPage.clickButton("Yes");

		uploadVirusFile("eicar-adobe-acrobat-attachment.pdf");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");
	}

	@Test
	void shouldDetectVirusInAllSupportedUploadFileTypes() throws InterruptedException, IOException {
		getToDocumentUploadScreen();

		// This set of tests all use the EICAR text file but with the filename extension changed.
		// 1. Upload a file with the .gif extension
		uploadVirusFile("EICAR.gif");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");

		// 2. Upload a file with the .jpg extension
		uploadVirusFile("EICAR.jpg");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");

		// 3. Upload a file with the .jpeg extension
		uploadVirusFile("EICAR.jpeg");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");

		// 4. Upload a file with the .png extension
		uploadVirusFile("EICAR.png");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");

		// 5. Upload a file with the .pdf extension
		uploadVirusFile("EICAR.pdf");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");
	}

	@Test
	void shouldDetectVirusInUploadFileWithAttachment() throws InterruptedException, IOException {
		getToDocumentUploadScreen();

		// This tests uploads a PDF that contains an attachment that has the EICAR virus
		uploadVirusFile("eicar-adobe-acrobat-attachment.pdf");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");
	}

	@Disabled("This test is disabled because it runs inconsistently. It needs analysis and a fix.")
	@Test
	void shouldDetectVirusInUploadFileWithVbaScript() throws InterruptedException, IOException {
		getToDocumentUploadScreen();

		// This tests uploads a PDF that contains a VBA script that runs and displays
		// the EICAR virus string
		uploadVirusFile("pdf-doc-vba-eicar-dropper.pdf");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");
	}

	@Test
	void shouldDetectVirusInMaxSizeUploadFile() throws InterruptedException, IOException {
		getToDocumentUploadScreen();

		// This tests uploads a maximum size PDF that contains an attachment that has
		// the EICAR virus
		uploadVirusFile("eicar-large.pdf");
		waitForErrorMessage();
		assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
				"Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
		assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains("0 files added");
		assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class")).contains("hidden");
		testPage.clickLink("remove");
	}

}
