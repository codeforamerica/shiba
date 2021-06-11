package org.codeforamerica.shiba.newjourneys;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.journeys.JourneyTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.By;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.application.FlowType.MINIMUM;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("journey")
public class MinimumSnapFlowJourneyTest extends JourneyTest {
    private PDAcroForm caf;

    private final String firstName = "Ahmed";
    private final String lastName = "St. George";
    private final String otherName = "defaultOtherName";
    private final String dateOfBirth = "01/12/1928";
    private final String sex = "Female";
    private final String moveDate = "10/20/1993";
    private final String previousCity = "Chicago";
    private final String needsInterpreter = "Yes";
    private final String email = "some@email.com";
    private final String homeZip = "12345";
    private final String homeCity = "someCity";
    private final String homeStreetAddress = "someStreetAddress";
    private final String homeApartmentNumber = "someApartmentNumber";
    private final String mailingStreetAddress = "smarty street";
    private final String mailingCity = "Cooltown";
    private final String mailingState = "CA";
    private final String mailingZip = "03104";
    private final String mailingApartmentNumber = "";
    private final String additionalInfo = "Some additional information about my application";
    private final String caseNumber = "654321";
    private final String signature = "some signature";

    @Test
    void nonExpeditedFlow() {
        getToMinimumFlow();

        // Minimum flow, don't answer expedited questions
        testPage.clickLink("Finish application now");

        String applicationId = finishApplicationAndDownloadCaf();
        assertCafContainsAllFieldsForMinimumSnapFlow(applicationId);
        assertApplicationSubmittedEventWasPublished(applicationId);
    }

    private void assertApplicationSubmittedEventWasPublished(String applicationId) {
        ArgumentCaptor<ApplicationSubmittedEvent> captor = ArgumentCaptor.forClass(ApplicationSubmittedEvent.class);
        verify(pageEventPublisher).publish(captor.capture());
        ApplicationSubmittedEvent applicationSubmittedEvent = captor.getValue();
        assertThat(applicationSubmittedEvent.getFlow()).isEqualTo(MINIMUM);
        assertThat(applicationSubmittedEvent.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationSubmittedEvent.getLocale()).isEqualTo(ENGLISH);
    }

    private void assertCafContainsAllFieldsForMinimumSnapFlow(String applicationId) {
        // Page 1
        assertPdfFieldEquals("APPLICATION_ID", applicationId);
        assertPdfFieldEquals("COUNTY_INSTRUCTIONS", "This application was submitted. A caseworker at Hennepin County will help route your application to your county. For more support with your application, you can call Hennepin County at 612-596-1300.");
        assertPdfFieldEquals("FULL_NAME", firstName + " " + lastName);
        assertPdfFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
        assertPdfFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "");
        assertPdfFieldEquals("ADDITIONAL_APPLICATION_INFO", additionalInfo);
        assertPdfFieldEquals("APPLICANT_EMAIL", email);
        assertPdfFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
        assertPdfFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", caseNumber);
        assertPdfFieldEquals("EMAIL_OPTIN", "Off");
        assertPdfFieldEquals("PHONE_OPTIN", "Yes");
        assertPdfFieldEquals("DATE_OF_BIRTH", dateOfBirth);
        assertPdfFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
        assertPdfFieldEquals("PROGRAMS", "SNAP");
        assertPdfFieldIsTodayWithFormat("SUBMISSION_DATETIME", "MM/dd/yyyy");

        // Page 5 and beyond
        assertPdfFieldEquals("APPLICANT_LAST_NAME", lastName);
        assertPdfFieldEquals("APPLICANT_FIRST_NAME", firstName);
        assertPdfFieldEquals("APPLICANT_OTHER_NAME", otherName);
        assertPdfFieldEquals("APPLICANT_SEX", sex.toUpperCase(ENGLISH));
        assertPdfFieldEquals("MARITAL_STATUS", "NEVER_MARRIED");
        assertPdfFieldEquals("APPLICANT_HOME_STREET_ADDRESS", homeStreetAddress + " (not permanent)");
        assertPdfFieldEquals("APPLICANT_HOME_APT_NUMBER", homeApartmentNumber);
        assertPdfFieldEquals("APPLICANT_HOME_CITY", homeCity);
        assertPdfFieldEquals("APPLICANT_HOME_STATE", "MN");
        assertPdfFieldEquals("APPLICANT_HOME_ZIPCODE", homeZip);
        assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", mailingStreetAddress);
        assertPdfFieldEquals("APPLICANT_MAILING_APT_NUMBER", mailingApartmentNumber);
        assertPdfFieldEquals("APPLICANT_MAILING_CITY", mailingCity);
        assertPdfFieldEquals("APPLICANT_MAILING_STATE", mailingState);
        assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", mailingZip);
        assertPdfFieldEquals("NEED_INTERPRETER", needsInterpreter);
        assertPdfFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertPdfFieldEquals("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertPdfFieldEquals("IS_US_CITIZEN", "Yes");
        assertPdfFieldEquals("DATE_OF_MOVING_TO_MN", moveDate);
        assertPdfFieldEquals("APPLICANT_PREVIOUS_STATE", previousCity);
        assertPdfFieldEquals("FOOD", "Yes");
        assertPdfFieldEquals("CASH", "Off");
        assertPdfFieldEquals("EMERGENCY", "Off");
        assertPdfFieldEquals("CCAP", "Off");
        assertPdfFieldEquals("GRH", "Off");
        assertPdfFieldEquals("DRUG_FELONY", "No");
        assertPdfFieldEquals("APPLICANT_SIGNATURE", signature);
        assertPdfFieldIsTodayWithFormat("CREATED_DATE", "yyyy-MM-dd");
    }

    private void getToMinimumFlow() {
        testPage.clickButton("Apply now");
        testPage.clickContinue();

        // Language Preferences
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", needsInterpreter);
        testPage.clickContinue();

        // Program Selection
        testPage.enter("programs", PROGRAM_SNAP);
        testPage.clickContinue();
        testPage.clickContinue();

        // Personal Info
        testPage.enter("firstName", firstName);
        testPage.enter("lastName", lastName);
        testPage.enter("otherName", otherName);
        testPage.enter("dateOfBirth", dateOfBirth);
        testPage.enter("ssn", "123456789");
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", sex);
        testPage.enter("livedInMnWholeLife", "Yes");
        testPage.enter("moveToMnDate", moveDate);
        testPage.enter("moveToMnPreviousCity", previousCity);
        testPage.clickContinue();

        // How can we get in touch with you?
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", email);
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();

        // Where are you currently Living?
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
        when(smartyStreetClient.validateAddress(any())).thenReturn(
                Optional.of(new Address(mailingStreetAddress, mailingCity, mailingState, mailingZip, mailingApartmentNumber, "someCounty"))
        );
        testPage.clickContinue();
        testPage.clickElementById("enriched-address");
        testPage.clickContinue();

        // Let's review your info
        assertThat(driver.findElementById("mailing-address_street").getText()).isEqualTo(mailingStreetAddress);

        testPage.clickLink("Submit application now with only the above information.");
    }

    private String finishApplicationAndDownloadCaf() {
        assertThat(testPage.getTitle()).isEqualTo("Additional Info");

        driver.findElement(By.id("additionalInfo")).sendKeys(additionalInfo);
        testPage.enter("caseNumber", caseNumber);
        testPage.clickContinue();

        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", NO.getDisplayValue());
        testPage.clickContinue();
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
        return successPage.getConfirmationNumber(); // Application ID
    }

    private void assertPdfFieldEquals(String fieldName, String expectedVal) {
        assertThat(getPdfFieldText(caf, fieldName)).isEqualTo(expectedVal);
    }

    private void assertPdfFieldIsTodayWithFormat(String fieldName, String dateFormat) {
        assertThat(getPdfFieldText(caf, fieldName)).contains(new SimpleDateFormat(dateFormat, ENGLISH).format(new Date()));
    }
}
