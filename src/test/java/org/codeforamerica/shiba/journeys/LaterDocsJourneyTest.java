package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@Tag("laterDocsJourney")
public class LaterDocsJourneyTest extends JourneyTest {

  
  @Test
  void laterDocsFlow() {
    when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.ON);
    when(featureFlagConfiguration.get("tribal-routing")).thenReturn(FeatureFlag.ON);
     testPage.clickButton("Upload documents");

    assertThat(driver.getTitle()).isEqualTo("Identify county or Tribal Nation");
    assertNotNull(testPage.findElementById("headerMNbenefits"));
    assertNotNull(testPage.findElementById("footerMNbenefits"));
//    testPage.clickLink("Enter my zip code instead.");
//    assertThat(driver.getTitle()).isEqualTo("Identify zip");
//
//    // should direct me to email the county if my zipcode is unrecognized or unsupported
//    testPage.enter("zipCode", "11111");
//    testPage.clickContinue();
//    assertThat(driver.getTitle()).isEqualTo("Email Docs To Your County");
//
//    // should allow me to proceed with the flow if I enter a zip code for an active county
//    testPage.clickLink("< Go Back");
//    testPage.enter("zipCode", "55444");
//    testPage.clickContinue();
//    assertThat(driver.getTitle()).isEqualTo("Match Info");
//
//    // should direct me to email docs to my county if my county is not supported
//    navigateTo("identifyCounty");

    testPage.enter("county", "Select your county");
    testPage.enter("tribalNation", "Select a Tribal Nation");
    testPage.clickContinue();
    assertThat(driver.getTitle()).isEqualTo("Identify county or Tribal Nation");
    // make sure that White Earth Nation routing logic is working
    // White Earth Nation shouldn't be allowed unless the county is also blank
    testPage.enter("county", "Hennepin");
    testPage.enter("tribalNation", "White Earth Nation");
    testPage.clickContinue();
    assertThat(driver.findElements(By.className("text--error")).get(0).getText()).contains(
            "Choose EITHER your county OR White Earth Nation, but not both. See instructions below.");
    // should allow me to enter personal info and continue the flow if my county is supported
    testPage.enter("county", "Hennepin");
    testPage.enter("tribalNation", "Red Lake Nation");
    testPage.clickContinue();

    assertThat(driver.getTitle()).isEqualTo("Match Info");
    // verify that the header & footer are the MNbenefits header & footer
    assertNotNull(testPage.findElementById("headerMNbenefits"));
    assertNotNull(testPage.findElementById("footerMNbenefits"));
    // verify that the chat feature does exist on the MNbenefits header
    assertFalse(testPage.elementDoesNotExistById("intercom_custom_launcher"));

    testPage.enter("firstName", "defaultFirstName");
    testPage.enter("lastName", "defaultLastName");
    testPage.enter("ssn", "123456789");
    testPage.enter("caseNumber", "1234567");
    testPage.enter("phoneNumber", "7041234567");
    testPage.clickContinue();
    testPage.clickContinue();

    // should allow me to upload documents and those documents should be sent to the ESB
    assertThat(driver.getTitle()).isEqualTo("Upload documents");
    assertNotNull(testPage.findElementById("headerMNbenefits"));
    assertNotNull(testPage.findElementById("footerMNbenefits"));
    assertThat(driver.findElements(By.className("reveal")).size()).isEqualTo(0);

    uploadPdfFile();
    waitForDocumentUploadToComplete();
    testPage.clickButton("Submit my documents");
    assertThat(driver.getTitle()).isEqualTo("Doc submit confirmation");
    testPage.clickButton("No, add more documents"); // Go back
    assertThat(driver.getTitle()).isEqualTo("Upload documents");

    testPage.clickButton("Submit my documents");
    testPage.clickButton("Yes, submit and finish");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");
    assertNotNull(testPage.findElementById("headerMNbenefits"));
    assertNotNull(testPage.findElementById("footerMNbenefits"));
    verify(pageEventPublisher).publish(any());

    // Assert that applicant can't resubmit docs at this point
    navigateTo("uploadDocuments");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");

    navigateTo("documentSubmitConfirmation");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");
  }
  
}
