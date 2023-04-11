package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.testutilities.PercyTestPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;


@Tag("HealthcareRenewalJourney")
public class HealthcareRenewalJourneyTest extends JourneyTest {

  protected void initTestPage() {
	    testPage = new PercyTestPage(driver);
	  }
  
  @Test
  void healthcareRenewalFlow() {
    when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.ON);
    when(featureFlagConfiguration.get("tribal-routing")).thenReturn(FeatureFlag.ON);
     //testPage.clickButton("Upload documents");
    navigateTo("healthcareRenewalUpload");
    assertThat(driver.getTitle()).isEqualTo("Health Care Renewal Document Upload");
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

    // should allow me to enter personal info and continue the flow if my county is supported
    testPage.enter("county", "Select your county");
    testPage.enter("tribalNation", "Select a Tribal Nation");
    testPage.clickContinue();
    assertThat(driver.getTitle()).isEqualTo("Health Care Renewal Document Upload");
    testPage.enter("county", "Hennepin");
    testPage.enter("tribalNation", "White Earth Nation");
    testPage.clickContinue();
    assertThat(driver.getTitle()).isEqualTo("Health Care Renewal Document Upload");
    testPage.enter("county", "Hennepin");
    testPage.enter("tribalNation", "Select a Tribal Nation");
    testPage.clickContinue();

    assertThat(driver.getTitle()).isEqualTo("Match Info");
    testPage.enter("firstName", "defaultFirstName");
    testPage.enter("lastName", "defaultLastName");
    testPage.enter("ssn", "123456789");
    testPage.enter("caseNumber", "123456789");//9 digits will cause error
    testPage.enter("phoneNumber", "7041234567");
    assertThat(testPage.getHeader()).isEqualTo("Before you start, we need to match your documents to your health care case");
    testPage.clickContinue();
    assertThat(driver.getTitle()).isEqualTo("Match Info");//stays on match info page
    testPage.enter("caseNumber", "123");//too short
    testPage.clickContinue();
    assertThat(driver.getTitle()).isEqualTo("Match Info");//stays on match info page
    testPage.enter("caseNumber", "12345678");
    testPage.clickContinue();
    testPage.clickContinue();

    // should allow me to upload documents and those documents should be sent to the ESB
    assertThat(driver.getTitle()).isEqualTo("Upload documents");
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
    verify(pageEventPublisher).publish(any());

    // Assert that applicant can't resubmit docs at this point
    navigateTo("uploadDocuments");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");

    navigateTo("documentSubmitConfirmation");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");
  }
  
}