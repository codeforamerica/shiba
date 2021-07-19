package org.codeforamerica.shiba.pages.journeys;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Tag("document")
public class DocumentsTest extends JourneyTest {
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
    void shouldUpdateFileCountWhenRemoveIsClickedIfAnUploadHasAnError() {
        getToDocumentUploadScreen();
        uploadJpgFile();
        driver.executeScript("$('#document-upload').get(0).dropzone.addFile({name: 'testFile.xyz', size: 1000, type: 'not-an-image'})");
        assertThat(driver.findElementsByClassName("text--error").get(0).getText()).contains("You can't upload files of this type");
        testPage.clickLink("remove");
        assertThat(driver.findElementById("number-of-uploaded-files").getText()).isEqualTo("1 file added");
    }
}
