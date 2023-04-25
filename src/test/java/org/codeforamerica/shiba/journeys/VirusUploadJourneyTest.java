package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Tag("virusUploadJourney")
public class VirusUploadJourneyTest extends JourneyTest {

  @Test
  void whenDocumentUploadVirusThereShouldBeAnError() throws InterruptedException, IOException {
    getToDocumentUploadScreen();
    uploadGIFVirusFile();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
        "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
        "0 files added");
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
    	.contains("hidden");
    testPage.clickLink("remove");

    uploadJPGVirusFile();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
        "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
        "0 files added");
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
    	.contains("hidden");
    testPage.clickLink("remove");

    uploadPNGVirusFile();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
        "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
        "0 files added");
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
    	.contains("hidden");
    testPage.clickLink("remove");
    
    uploadPDFVirusFile1();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
        "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
        "0 files added");
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
    	.contains("hidden");
    testPage.clickLink("remove");

    uploadPDFVirusFile2();
    waitForErrorMessage();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
        "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
        "0 files added");
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
    	.contains("hidden");
    testPage.clickLink("remove");
//    TODO, the EICAR_PDF_VIRUS_3 = "pdf-doc-vba-eicar-dropper.pdf"; is no longer recognized as a virus
//    uploadJpgFile();
//    uploadPDFVirusFile3();
//    waitForErrorMessage();
//    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
//            "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
//    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
//        "1 file added");
//    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
//    	.contains("disabled");
//    uploadPDFVirusFile3();
//    waitForErrorMessage();
//    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
//            "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
//    assertThat(driver.findElements(By.className("text--error")).get(1).getText()).contains(
//            "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
//    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
//        "1 file added");
//    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
//    	.contains("disabled");
//    testPage.clickLink("remove");
//    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
//            "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
//    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
//		.contains("disabled");
//    testPage.clickLink("remove");
//    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
//    	.doesNotContain("disabled");
//    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
//	.doesNotContain("hidden");
//    testPage.clickLink("delete");
//    testPage.clickButton("Yes");
//    
//    
//    uploadPDFVirusFile3();
//    waitForErrorMessage();
//    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
//        "Your file cannot be uploaded because a virus was detected. Try uploading a different copy of the file.");
//    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText()).contains(
//        "0 files added");
//    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
//    	.contains("hidden");
//    testPage.clickLink("remove");
  }
}
