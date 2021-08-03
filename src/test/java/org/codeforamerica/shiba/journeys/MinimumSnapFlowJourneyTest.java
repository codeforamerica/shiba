package org.codeforamerica.shiba.journeys;

import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.List;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.application.FlowType.EXPEDITED;
import static org.codeforamerica.shiba.application.FlowType.MINIMUM;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.NO;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.mockito.Mockito.when;

@Tag("journey")
public class MinimumSnapFlowJourneyTest extends JourneyTest {
    private final String mailingStreetAddress = "smarty street";
    private final String mailingCity = "Cooltown";
    private final String mailingState = "CA";
    private final String mailingZip = "03104";
    private final String mailingApartmentNumber = "1b";
    private final String signature = "some signature";

    @Test
    void nonExpeditedFlow() {
        // No permanent address for this test
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.ON);

        getToHomeAddress(List.of(PROGRAM_SNAP));

        // Where are you currently Living? (with home address)
        testPage.enter("zipCode", "23456");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("Mailing address");
        testPage.goBack();

        // Where are you currently Living? (without address)
        testPage.enter("isHomeless", "I don't have a permanent address"); // check
        testPage.enter("isHomeless", "I don't have a permanent address"); // uncheck
        testPage.clickContinue();
        assertThat(driver.findElementById("state").getAttribute("value")).isEqualTo("MN");
        assertThat(testPage.hasInputError("streetAddress")).isTrue(); // verify cleared previous inputs
        testPage.enter("isHomeless", "I don't have a permanent address"); // check
        testPage.clickContinue();

        // General Delivery
        testPage.clickLink("I will pick up mail at a General Delivery post office near me.");
        assertThat(testPage.getTitle()).isEqualTo("City for General Delivery");
        testPage.clickContinue(); // Error on "Continue" without selecting a city
        assertThat(testPage.hasErrorText("Make sure to provide a city")).isTrue();
        testPage.selectFromDropdown("whatIsTheCity[]", "Ada");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("General Delivery address");

        // Let's review your info
        testPage.clickContinue();
        assertThat(driver.findElementById("homeAddress-address_message").getText()).isEqualTo("No permanent address");
        assertThat(testPage.findElementById("generalDelivery_streetAddress").getText()).isEqualTo("Ada, MN");

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
        applicationId = signApplicationAndDownloadPdfs(signature, true, false);
        assertApplicationSubmittedEventWasPublished(applicationId, MINIMUM, 1);

        // PDF assertions
        assertCafContainsAllFieldsForMinimumSnapFlow(applicationId);
        assertCafFieldEquals("MEDICAL_EXPENSES_SELECTION", "Off");
        assertCafFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
        assertCafFieldEquals("DRUG_FELONY", "No");
        assertCafFieldEquals("ADDITIONAL_APPLICATION_INFO", additionalInfo);
        assertCafFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", caseNumber);
        assertCafFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "No permanent address");
        assertCafFieldEquals("APPLICANT_HOME_APT_NUMBER", "");
        assertCafFieldEquals("APPLICANT_HOME_CITY", "");
        assertCafFieldEquals("APPLICANT_HOME_STATE", "MN");
        assertCafFieldEquals("APPLICANT_HOME_ZIPCODE", "");
        assertCafFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", "General Delivery");
        assertCafFieldEquals("APPLICANT_MAILING_APT_NUMBER", "");
        assertCafFieldEquals("APPLICANT_MAILING_CITY", "Ada");
        assertCafFieldEquals("APPLICANT_MAILING_STATE", "MN");
        assertCafFieldEquals("APPLICANT_MAILING_ZIPCODE", "56510-9999");
    }

    @Test
    void expeditedFlow() {
        getToHomeAddress(List.of(PROGRAM_SNAP));

        // Where are you currently Living?
        String homeZip = "12345";
        String homeCity = "someCity";
        String homeStreetAddress = "someStreetAddress";
        String homeApartmentNumber = "someApartmentNumber";
        fillOutHomeAndMailingAddress(homeZip, homeCity, homeStreetAddress, homeApartmentNumber);

        testPage.clickLink("Submit application now with only the above information.");

        // Answer expedited questions such that we will be expedited
        testPage.clickLink("Yes, I want to see if I qualify");

        // Add Household Members
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());

        // How much money has your household made in the last 30 days?
        assertThat(driver.findElement(By.cssSelector("h1")).getText())
                .isEqualTo("How much money has your household made in the last 30 days?");
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
        assertThat(driver.findElement(By.tagName("p")).getText()).contains(
                "Your county should reach out to you for your interview within 24 hours.");
        testPage.clickButton("Finish application");

        // Legal Stuff
        assertThat(testPage.getTitle()).isEqualTo("Legal Stuff");
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", YES.getDisplayValue());
        testPage.clickContinue();

        // Finish Application
        applicationId = signApplicationAndDownloadPdfs(signature, true, false);
        assertApplicationSubmittedEventWasPublished(applicationId, EXPEDITED, 1);

        // PDF assertions
        assertCafContainsAllFieldsForMinimumSnapFlow(applicationId);
        assertCafFieldEquals("MEDICAL_EXPENSES_SELECTION", "Off");
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
        assertCafFieldEquals("APPLICANT_HOME_STREET_ADDRESS", homeStreetAddress);
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


    private void assertCafContainsAllFieldsForMinimumSnapFlow(String applicationId) {
        // Page 1
        assertCafFieldEquals("APPLICATION_ID", applicationId);
        assertCafFieldEquals("COUNTY_INSTRUCTIONS",
                "This application was submitted. A caseworker at Hennepin County will help route your application to your county. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed. For more support with your application, you can call Hennepin County at 612-596-1300.");
        assertCafFieldEquals("FULL_NAME", "Ahmed St. George");
        assertCafFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "");
        assertCafFieldEquals("APPLICANT_EMAIL", "some@example.com");
        assertCafFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
        assertCafFieldEquals("EMAIL_OPTIN", "Off");
        assertCafFieldEquals("PHONE_OPTIN", "Yes");
        assertCafFieldEquals("DATE_OF_BIRTH", "01/12/1928");
        assertCafFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
        assertCafFieldEquals("PROGRAMS", "SNAP");

        // Page 5 and beyond
        assertCafFieldEquals("APPLICANT_LAST_NAME", "St. George");
        assertCafFieldEquals("APPLICANT_FIRST_NAME", "Ahmed");
        String otherName = "defaultOtherName";
        assertCafFieldEquals("APPLICANT_OTHER_NAME", otherName);
        String sex = "Female";
        assertCafFieldEquals("APPLICANT_SEX", sex.toUpperCase(ENGLISH));
        assertCafFieldEquals("MARITAL_STATUS", "NEVER_MARRIED");
        String needsInterpreter = "Yes";
        assertCafFieldEquals("NEED_INTERPRETER", needsInterpreter);
        assertCafFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertCafFieldEquals("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE", "ENGLISH");
        assertCafFieldEquals("IS_US_CITIZEN", "Yes");
        String moveDate = "10/20/1993";
        assertCafFieldEquals("DATE_OF_MOVING_TO_MN", moveDate);
        String previousCity = "Chicago";
        assertCafFieldEquals("APPLICANT_PREVIOUS_STATE", previousCity);
        assertCafFieldEquals("FOOD", "Yes");
        assertCafFieldEquals("CASH", "Off");
        assertCafFieldEquals("EMERGENCY", "Off");
        assertCafFieldEquals("CCAP", "Off");
        assertCafFieldEquals("GRH", "Off");
        assertCafFieldEquals("APPLICANT_SIGNATURE", signature);
    }
}
