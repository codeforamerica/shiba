package org.codeforamerica.shiba.newjourneys;

import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.journeys.JourneyTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Optional;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.application.FlowType.EXPEDITED;
import static org.codeforamerica.shiba.application.FlowType.MINIMUM;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("journey")
public class MinimumSnapFlowJourneyTest extends JourneyTest {
    private final String firstName = "Ahmed";
    private final String lastName = "St. George";
    private final String otherName = "defaultOtherName";
    private final String dateOfBirth = "01/12/1928";
    private final String sex = "Female";
    private final String moveDate = "10/20/1993";
    private final String previousCity = "Chicago";
    private final String needsInterpreter = "Yes";
    private final String email = "some@email.com";
    private final String mailingStreetAddress = "smarty street";
    private final String mailingCity = "Cooltown";
    private final String mailingState = "CA";
    private final String mailingZip = "03104";
    private final String mailingApartmentNumber = "";
    private final String signature = "some signature";

    @Test
    void nonExpeditedFlow() {
        // No permanent address for this test
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.ON);

        getToHomeAddress(dateOfBirth, email, firstName, lastName, moveDate, needsInterpreter, otherName, previousCity, sex, testPage, List.of(PROGRAM_SNAP));

        // Where are you currently Living? (with home address)
        testPage.enter("zipCode", "23456");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Address Validation");
        testPage.goBack();

        // Where are you currently Living? (without address)
        testPage.enter("isHomeless", "I don't have a permanent address"); // check
        testPage.enter("isHomeless", "I don't have a permanent address"); // uncheck
        testPage.clickContinue();
        assertThat(driver.findElementById("state").getAttribute("value")).isEqualTo("MN");
        assertThat(testPage.hasInputError("streetAddress")).isTrue(); // verify cleared previous inputs
        testPage.enter("isHomeless", "I don't have a permanent address"); // check
        testPage.clickContinue();

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

        // Opt not to answer expedited questions
        testPage.clickLink("Finish application now");

        // Additional Info
        assertThat(testPage.getTitle()).isEqualTo("Additional Info");
        String additionalInfo = "Some additional information about my application";
        String caseNumber = "654321";
        driver.findElement(By.id("additionalInfo")).sendKeys(additionalInfo);
        testPage.enter("caseNumber", caseNumber);
        testPage.clickContinue();

        // Legal Stuff
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", NO.getDisplayValue());
        testPage.clickContinue();

        // Finish Application
        String applicationId = signApplicationAndDownloadCaf(signature);
        assertApplicationSubmittedEventWasPublished(applicationId, MINIMUM);

        // PDF assertions
        assertCafContainsAllFieldsForMinimumSnapFlow(applicationId);
        assertCafFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
        assertCafFieldEquals("DRUG_FELONY", "No");
        assertCafFieldEquals("ADDITIONAL_APPLICATION_INFO", additionalInfo);
        assertCafFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", caseNumber);
        assertCafFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "No permanent address");
        assertCafFieldEquals("APPLICANT_HOME_APT_NUMBER", "");
        assertCafFieldEquals("APPLICANT_HOME_CITY", "");
        assertCafFieldEquals("APPLICANT_HOME_STATE", "");
        assertCafFieldEquals("APPLICANT_HOME_ZIPCODE", "");
        // mailing address is currently not being filled in when the feature flag is on
        //        assertPdfFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", mailingStreetAddress);
        //        assertPdfFieldEquals("APPLICANT_MAILING_APT_NUMBER", mailingApartmentNumber);
        //        assertPdfFieldEquals("APPLICANT_MAILING_CITY", mailingCity);
        //        assertPdfFieldEquals("APPLICANT_MAILING_STATE", mailingState);
        //        assertPdfFieldEquals("APPLICANT_MAILING_ZIPCODE", mailingZip);
    }

    @Test
    void expeditedFlow() {
        getToHomeAddress(dateOfBirth, email, firstName, lastName, moveDate, needsInterpreter, otherName, previousCity, sex, testPage, List.of(PROGRAM_SNAP));

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
        when(smartyStreetClient.validateAddress(any())).thenReturn(
                Optional.of(new Address(mailingStreetAddress, mailingCity, mailingState, mailingZip, mailingApartmentNumber, "someCounty"))
        );
        testPage.clickContinue();
        testPage.clickElementById("enriched-address");
        testPage.clickContinue();

        // Let's review your info
        assertThat(driver.findElementById("mailing-address_street").getText()).isEqualTo(mailingStreetAddress);

        testPage.clickLink("Submit application now with only the above information.");

        // Answer expedited questions such that we will be expedited
        testPage.clickLink("Yes, I want to see if I qualify");

        // Add Household Members
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());

        // How much money has your household made in the last 30 days?
        assertThat(driver.findElement(By.cssSelector("h1")).getText()).isEqualTo("How much money has your household made in the last 30 days?");
        String moneyMadeLast30Days = "1";
        testPage.enter("moneyMadeLast30Days", moneyMadeLast30Days);
        testPage.clickContinue();

        // Do you have savings?
        testPage.enter("haveSavings", YES.getDisplayValue());
        String liquidAssets = "1";
        testPage.enter("liquidAssets", liquidAssets);
        testPage.clickContinue();

        // Home expenses
        testPage.enter("payRentOrMortgage", YES.getDisplayValue());
        String homeExpensesAmount = "333";
        testPage.enter("homeExpensesAmount", homeExpensesAmount);
        testPage.clickContinue();

        // Utilities
        testPage.enter("payForUtilities", "Cooling");
        testPage.clickContinue();

        // Migrant or Seasonal worker
        String migrantOrSeasonalFarmWorker = NO.getDisplayValue();
        testPage.enter("migrantOrSeasonalFarmWorker", migrantOrSeasonalFarmWorker);

        // You are expedited!
        assertThat(driver.findElement(By.tagName("p")).getText()).contains("Your county should reach out to you for your interview within 24 hours.");
        testPage.clickButton("Finish application");

        // Legal Stuff
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", YES.getDisplayValue());
        testPage.clickContinue();

        // Finish Application
        String applicationId = signApplicationAndDownloadCaf(signature);
        assertApplicationSubmittedEventWasPublished(applicationId, EXPEDITED);

        // PDF assertions
        assertCafContainsAllFieldsForMinimumSnapFlow(applicationId);
        assertCafFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "SNAP");
        assertCafFieldEquals("DRUG_FELONY", "Yes");
        assertCafFieldEquals("MONEY_MADE_LAST_MONTH", moneyMadeLast30Days + ".00");
        assertCafFieldEquals("EXPEDITED_QUESTION_2", liquidAssets);
        assertCafFieldEquals("HOUSING_EXPENSES", homeExpensesAmount);
        assertCafFieldEquals("HEAT", "No");
        assertCafFieldEquals("AIR_CONDITIONING", "Yes");
        assertCafFieldEquals("ELECTRICITY", "No");
        assertCafFieldEquals("PHONE", "No");
        assertCafFieldEquals("NO_EXPEDITED_UTILITIES_SELECTED", "Off");
        assertCafFieldEquals("MIGRANT_SEASONAL_FARM_WORKER", migrantOrSeasonalFarmWorker);
        assertCafFieldEquals("HEATING_COOLING_SELECTION", "ONE_SELECTED");
        assertCafFieldEquals("WATER_SEWER_SELECTION", "NEITHER_SELECTED");
        assertCafFieldEquals("COOKING_FUEL", "No");
        assertCafFieldEquals("HAVE_SAVINGS", "Yes");
        assertCafFieldEquals("APPLICANT_HOME_STREET_ADDRESS", homeStreetAddress + " (not permanent)");
        assertCafFieldEquals("APPLICANT_HOME_APT_NUMBER", homeApartmentNumber);
        assertCafFieldEquals("APPLICANT_HOME_CITY", homeCity);
        assertCafFieldEquals("APPLICANT_HOME_STATE", "MN");
        assertCafFieldEquals("APPLICANT_HOME_ZIPCODE", homeZip);
        assertCafFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", mailingStreetAddress);
        assertCafFieldEquals("APPLICANT_MAILING_APT_NUMBER", mailingApartmentNumber);
        assertCafFieldEquals("APPLICANT_MAILING_CITY", mailingCity);
        assertCafFieldEquals("APPLICANT_MAILING_STATE", mailingState);
        assertCafFieldEquals("APPLICANT_MAILING_ZIPCODE", mailingZip);
    }

    private void assertApplicationSubmittedEventWasPublished(String applicationId, FlowType flowType) {
        ArgumentCaptor<ApplicationSubmittedEvent> captor = ArgumentCaptor.forClass(ApplicationSubmittedEvent.class);
        verify(pageEventPublisher).publish(captor.capture());
        ApplicationSubmittedEvent applicationSubmittedEvent = captor.getValue();
        assertThat(applicationSubmittedEvent.getFlow()).isEqualTo(flowType);
        assertThat(applicationSubmittedEvent.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationSubmittedEvent.getLocale()).isEqualTo(ENGLISH);
    }

    private void assertCafContainsAllFieldsForMinimumSnapFlow(String applicationId) {
        // Page 1
        assertCafFieldEquals("APPLICATION_ID", applicationId);
        assertCafFieldEquals("COUNTY_INSTRUCTIONS", "This application was submitted. A caseworker at Hennepin County will help route your application to your county. For more support with your application, you can call Hennepin County at 612-596-1300.");
        assertCafFieldEquals("FULL_NAME", firstName + " " + lastName);
        assertCafFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "");
        assertCafFieldEquals("APPLICANT_EMAIL", email);
        assertCafFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
        assertCafFieldEquals("EMAIL_OPTIN", "Off");
        assertCafFieldEquals("PHONE_OPTIN", "Yes");
        assertCafFieldEquals("DATE_OF_BIRTH", dateOfBirth);
        assertCafFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
        assertCafFieldEquals("PROGRAMS", "SNAP");

        // Page 5 and beyond
        assertCafFieldEquals("APPLICANT_LAST_NAME", lastName);
        assertCafFieldEquals("APPLICANT_FIRST_NAME", firstName);
        assertCafFieldEquals("APPLICANT_OTHER_NAME", otherName);
        assertCafFieldEquals("APPLICANT_SEX", sex.toUpperCase(ENGLISH));
        assertCafFieldEquals("MARITAL_STATUS", "NEVER_MARRIED");
        assertCafFieldEquals("NEED_INTERPRETER", needsInterpreter);
        assertCafFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertCafFieldEquals("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertCafFieldEquals("IS_US_CITIZEN", "Yes");
        assertCafFieldEquals("DATE_OF_MOVING_TO_MN", moveDate);
        assertCafFieldEquals("APPLICANT_PREVIOUS_STATE", previousCity);
        assertCafFieldEquals("FOOD", "Yes");
        assertCafFieldEquals("CASH", "Off");
        assertCafFieldEquals("EMERGENCY", "Off");
        assertCafFieldEquals("CCAP", "Off");
        assertCafFieldEquals("GRH", "Off");
        assertCafFieldEquals("APPLICANT_SIGNATURE", signature);
    }
}
