package org.codeforamerica.shiba.journeys;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Tag("journey")
public class DocumentUploadJourneyTest extends JourneyTest {
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
    void shouldShowErrorsAndWarningsProperly() {
        getToDocumentUploadScreen();
        uploadJpgFile();

        // should disallow adding files with types that are not on the allow list
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.xyz', size: 1000, type: 'not-an-image'})");
        assertThat(driver.findElementsByClassName("text--error").get(0).getText()).contains("You can't upload files of this type");
        testPage.clickLink("remove");
        assertThat(driver.findElementById("number-of-uploaded-files").getText()).isEqualTo("1 file added");

        // should show max filesize error message for files that are too big
        long largeFilesize = 21000000L;
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.pdf', size: " + largeFilesize + ", type: 'not-an-image'})");
        int maxFileSize = uploadDocumentConfiguration.getMaxFilesize();
        assertThat(driver.findElementByClassName("text--error").getText()).contains("This file is too large and cannot be uploaded (max size: " + maxFileSize + " MB)");
        testPage.clickLink("remove");
        assertThat(driver.findElementById("number-of-uploaded-files").getText()).isEqualTo("1 file added");

        // should alert the user when they have uploaded the maximum number of files
        IntStream.range(0, 19).forEach(c -> uploadJpgFile());
        assertThat(driver.findElementById("max-files").getText()).contains("You have uploaded the maximum number of files (20). You will have the opportunity to share more with a county worker later.");
    }
}
