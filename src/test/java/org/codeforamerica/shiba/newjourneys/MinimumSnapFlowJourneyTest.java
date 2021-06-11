package org.codeforamerica.shiba.newjourneys;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.journeys.JourneyTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("journey")
public class MinimumSnapFlowJourneyTest extends JourneyTest {
    private PDAcroForm caf;

    @Test
    void minimumFlowForSnapOnly() {
        testPage.clickButton("Apply now");
        testPage.clickContinue();

        // Language Preferences
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", "Yes");
        testPage.clickContinue();

        // Program Selection
        testPage.enter("programs", PROGRAM_SNAP);
        testPage.clickContinue();
        testPage.clickContinue();

        // Personal Info
        String firstName = "Ahmed";
        testPage.enter("firstName", firstName);
        String lastName = "St. George";
        testPage.enter("lastName", lastName);
        String otherName = "defaultOtherName";
        testPage.enter("otherName", otherName);
        String dateOfBirth = "01/12/1928";
        testPage.enter("dateOfBirth", dateOfBirth);
        String ssn = "123456789";
        testPage.enter("ssn", ssn);
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", "Female");
        testPage.enter("livedInMnWholeLife", "Yes");
        testPage.enter("moveToMnDate", "02/18/1776");
        testPage.enter("moveToMnPreviousCity", "Chicago");
        testPage.clickContinue();

        // How can we get in touch with you?
        String phoneNumber = "7234567890";
        testPage.enter("phoneNumber", phoneNumber);
        String email = "some@email.com";
        testPage.enter("email", email);
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();

        // Where are you currently Living?
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        testPage.enter("isHomeless", "I don't have a permanent address");
        testPage.enter("sameMailingAddress", "No, use a different address for mail");
        testPage.clickContinue();
        testPage.clickButton("Use this address");

        // Where can the county send your mail?
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("state", "IL");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        String mailingAddressStreet = "smarty street";
        when(smartyStreetClient.validateAddress(any())).thenReturn(
                Optional.of(new Address(mailingAddressStreet, "City", "CA", "03104", "", "someCounty"))
        );
        testPage.clickContinue();
        testPage.clickElementById("enriched-address");
        testPage.clickContinue();

        // Let's review your info
        assertThat(driver.findElementById("mailing-address_street").getText()).isEqualTo(mailingAddressStreet);

        // Minimum flow, don't answer expedited questions
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Finish application now");

        assertThat(testPage.getTitle()).isEqualTo("Additional Info");
        driver.findElement(By.id("additionalInfo")).sendKeys("Some additional information about my application");
        String caseNumber = "654321";
        testPage.enter("caseNumber", caseNumber);
        testPage.clickContinue();

        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        // No document upload
        testPage.clickButton("Skip this for now");

        // Download CAF
        SuccessPage successPage = new SuccessPage(driver);
        assertThat(successPage.CAFdownloadPresent()).isTrue();
        assertThat(successPage.CCAPdownloadPresent()).isFalse();
        successPage.downloadPdfs();
        await().until(() -> getAllFiles().size() == successPage.pdfDownloadLinks());
        caf = getAllFiles().get(CAF);
        String applicationId = successPage.getConfirmationNumber();

        // assert that CAF contains expected values

        assertFieldEquals("APPLICATION_ID", applicationId);
        assertFieldEquals("FULL_NAME", firstName + " " + lastName);
        //TODO SUBMISSION_DATETIME?
        assertFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
        assertFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "");
        assertFieldEquals("ADDITIONAL_APPLICATION_INFO", "Some additional information about my application");
        assertFieldEquals("APPLICANT_EMAIL", email);
        assertFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
        assertFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", caseNumber);
        assertFieldEquals("EMAIL_OPTIN", "Off");
        assertFieldEquals("PHONE_OPTIN", "Yes");

    }

    private void assertFieldEquals(String fieldName, String expectedVal) {
        assertThat(getPdfFieldText(caf, fieldName)).isEqualTo(expectedVal);
    }
}
