package org.codeforamerica.shiba.newjourneys;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.journeys.JourneyTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
        String needsInterpreter = "Yes";
        testPage.enter("needInterpreter", needsInterpreter);
        testPage.clickContinue();

        // Program Selection
        testPage.enter("programs", PROGRAM_SNAP);
        testPage.clickContinue();
        testPage.clickContinue();

        // Personal Info
        String firstName = "Ahmed";
        String lastName = "St. George";
        String otherName = "defaultOtherName";
        String dateOfBirth = "01/12/1928";
        String ssn = "123456789";
        String sex = "Female";
        String moveDate = "10/20/1993";
        String previousCity = "Chicago";
        testPage.enter("firstName", firstName);
        testPage.enter("lastName", lastName);
        testPage.enter("otherName", otherName);
        testPage.enter("dateOfBirth", dateOfBirth);
        testPage.enter("ssn", ssn);
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", sex);
        testPage.enter("livedInMnWholeLife", "Yes");
        testPage.enter("moveToMnDate", moveDate);
        testPage.enter("moveToMnPreviousCity", previousCity);
        testPage.clickContinue();

        // How can we get in touch with you?
        String phoneNumber = "7234567890";
        String email = "some@email.com";
        testPage.enter("phoneNumber", phoneNumber);
        testPage.enter("email", email);
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();

        // Where are you currently Living?
        String homeZip = "12345";
        String homeCity = "someCity";
        String homeStreetAddress = "someStreetAddress";
        String homeApartmentNumber = "someApartmentNumber";
        testPage.enter("zipCode", homeZip);
        testPage.enter("city", homeCity);
        testPage.enter("streetAddress", homeStreetAddress);
        testPage.enter("apartmentNumber", homeApartmentNumber);
        testPage.enter("isHomeless", "I don't have a permanent address");
        testPage.enter("sameMailingAddress", "No, use a different address for mail");
        testPage.clickContinue();
        testPage.clickButton("Use this address");

        // Where can the county send your mail? (accept the smarty streets enriched address)
        testPage.enter("zipCode", "23456");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("state", "IL");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        String mailingStreetAddress = "smarty street";
        String mailingCity = "Cooltown";
        String mailingState = "CA";
        String mailingZip = "03104";
        String mailingApartmentNumber = "";
        String mailingCounty = "someCounty";
        when(smartyStreetClient.validateAddress(any())).thenReturn(
                Optional.of(new Address(mailingStreetAddress, mailingCity, mailingState, mailingZip, mailingApartmentNumber, mailingCounty))
        );
        testPage.clickContinue();
        testPage.clickElementById("enriched-address");
        testPage.clickContinue();

        // Let's review your info
        assertThat(driver.findElementById("mailing-address_street").getText()).isEqualTo(mailingStreetAddress);

        // Minimum flow, don't answer expedited questions
        testPage.clickLink("Submit application now with only the above information.");
        testPage.clickLink("Finish application now");

        assertThat(testPage.getTitle()).isEqualTo("Additional Info");
        String additionalInfo = "Some additional information about my application";
        driver.findElement(By.id("additionalInfo")).sendKeys(additionalInfo);
        String caseNumber = "654321";
        testPage.enter("caseNumber", caseNumber);
        testPage.clickContinue();

        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", NO.getDisplayValue());
        testPage.clickContinue();
        String signature = "some signature";
        testPage.enter("applicantSignature", signature);
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

        // ASSERT THAT CAF CONTAINS EXPECTED VALUES

        // Page 1
        assertFieldEquals("APPLICATION_ID", applicationId);
        assertFieldEquals("FULL_NAME", firstName + " " + lastName);
        assertFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
        assertFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "");
        assertFieldEquals("ADDITIONAL_APPLICATION_INFO", additionalInfo);
        assertFieldEquals("APPLICANT_EMAIL", email);
        assertFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
        assertFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", caseNumber);
        assertFieldEquals("EMAIL_OPTIN", "Off");
        assertFieldEquals("PHONE_OPTIN", "Yes");
        assertFieldEquals("DATE_OF_BIRTH", dateOfBirth);
        assertFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
        assertFieldEquals("PROGRAMS", "SNAP");
        assertDateFieldIsTodayWithFormat("SUBMISSION_DATETIME", "MM/dd/yyyy");

        // Page 5 and beyond
        assertFieldEquals("APPLICANT_LAST_NAME", lastName);
        assertFieldEquals("APPLICANT_FIRST_NAME", firstName);
        assertFieldEquals("APPLICANT_OTHER_NAME", otherName);
        assertFieldEquals("APPLICANT_SEX", sex.toUpperCase(Locale.ENGLISH));
        assertFieldEquals("MARITAL_STATUS", "NEVER_MARRIED");
        assertFieldEquals("APPLICANT_HOME_STREET_ADDRESS", homeStreetAddress + " (not permanent)");
        assertFieldEquals("APPLICANT_HOME_APT_NUMBER", homeApartmentNumber);
        assertFieldEquals("APPLICANT_HOME_CITY", homeCity);
        assertFieldEquals("APPLICANT_HOME_STATE", "MN");
        assertFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", mailingStreetAddress);
        assertFieldEquals("APPLICANT_MAILING_APT_NUMBER", mailingApartmentNumber);
        assertFieldEquals("APPLICANT_MAILING_CITY", mailingCity);
        assertFieldEquals("APPLICANT_MAILING_STATE", mailingState);
        assertFieldEquals("APPLICANT_MAILING_ZIPCODE", mailingZip);
        assertFieldEquals("NEED_INTERPRETER", needsInterpreter);
        assertFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertFieldEquals("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertFieldEquals("IS_US_CITIZEN", "Yes");
        assertFieldEquals("DATE_OF_MOVING_TO_MN", moveDate);
        assertFieldEquals("APPLICANT_PREVIOUS_STATE", previousCity);
        assertFieldEquals("FOOD", "Yes");
        assertFieldEquals("CASH", "Off");
        assertFieldEquals("EMERGENCY", "Off");
        assertFieldEquals("CCAP", "Off");
        assertFieldEquals("GRH", "Off");
        assertFieldEquals("DRUG_FELONY", "No");
        assertFieldEquals("APPLICANT_SIGNATURE", signature);
        assertDateFieldIsTodayWithFormat("CREATED_DATE", "yyyy-MM-dd");
    }

    private void assertFieldEquals(String fieldName, String expectedVal) {
        assertThat(getPdfFieldText(caf, fieldName)).isEqualTo(expectedVal);
    }

    private void assertDateFieldIsTodayWithFormat(String submission_datetime, String s) {
        assertThat(getPdfFieldText(caf, submission_datetime)).contains(new SimpleDateFormat(s, Locale.ENGLISH).format(new Date()));
    }
}
