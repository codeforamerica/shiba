package org.codeforamerica.shiba.pages.journeys;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import org.codeforamerica.shiba.pages.AccessibilityTestPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("a11y")
public class AccessibilityJourneyPageTest extends JourneyTest {
    protected static List<Rule> resultsList = new ArrayList<>();
    protected static Results results;
    protected AccessibilityTestPage testPage;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        testPage = new AccessibilityTestPage(driver);
    }

    @AfterEach
    void afterEach() {
        AxeBuilder builder = new AxeBuilder();
        results = builder.analyze(driver);
        resultsList.addAll(testPage.resultsList);
    }

    @AfterAll
    static void tearDownAll() {
        generateAccessibilityReport(results);
    }

    @Test
    void laterDocsFlow() {
        when(featureFlagConfiguration.get("county-hennepin")).thenReturn(FeatureFlag.ON);
        when(featureFlagConfiguration.get("county-morrison")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.ON);

        testPage.clickButton("Upload documents");

        testPage.clickLink("Enter my zip code instead.");

        // should direct me to email the county if my zipcode is unrecognized or unsupported
        testPage.enter("zipCode", "11111");
        testPage.clickContinue();

        // should allow me to proceed with the flow if I enter a zip code for an active county
        testPage.clickLink("< Go Back");
        testPage.enter("zipCode", "55444");
        testPage.clickContinue();

        // should direct me to email docs to my county if my county is not supported
        navigateTo("identifyCounty");
        testPage.enter("county", "Morrison");
        testPage.clickContinue();

        // should allow me to enter personal info and continue the flow if my county is supported
        testPage.clickLink("< Go Back");
        testPage.enter("county", "Hennepin");
        testPage.clickContinue();

        testPage.enter("firstName", "defaultFirstName");
        testPage.enter("lastName", "defaultLastName");
        testPage.enter("dateOfBirth", "01/12/1928");
        testPage.enter("ssn", "123456789");
        testPage.enter("caseNumber", "1234567");
        testPage.clickContinue();

        // should allow me to upload documents and those documents should be sent to the ESB
        uploadPdfFile();
        await().until(uploadCompletes());
        testPage.clickButton("I'm finished uploading");
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlow() {
        List<String> programSelections = List.of(PROGRAM_SNAP, PROGRAM_CCAP);

        testPage.clickButton("Apply now");
        testPage.clickContinue();
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", "Yes");
        testPage.clickContinue();
        programSelections.forEach(program -> testPage.enter("programs", program));
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("firstName", "defaultFirstName");
        testPage.enter("lastName", "defaultLastName");
        testPage.enter("otherName", "defaultOtherName");
        testPage.enter("dateOfBirth", "01/12/1928");
        testPage.enter("ssn", "123456789");
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", "Female");
        testPage.enter("livedInMnWholeLife", "Yes");
        testPage.enter("moveToMnDate", "02/18/1776");
        testPage.enter("moveToMnPreviousCity", "Chicago");
        testPage.clickContinue();
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", "some@email.com");
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        testPage.enter("isHomeless", "I don't have a permanent address");
        testPage.enter("sameMailingAddress", "No, use a different address for mail");
        testPage.clickContinue();
        testPage.clickButton("Use this address");
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("state", "IL");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        when(smartyStreetClient.validateAddress(any())).thenReturn(
                Optional.of(new Address("smarty street", "City", "CA", "03104", "", "someCounty"))
        );
        testPage.clickContinue();
        testPage.clickElementById("enriched-address");
        testPage.clickContinue();
        testPage.clickLink("This looks correct");
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("relationship", "housemate");
        testPage.enter("programs", PROGRAM_CCAP);
        testPage.enter("firstName", "householdMemberFirstName");
        testPage.enter("lastName", "householdMemberLastName");
        testPage.enter("otherName", "houseHoldyMcMemberson");
        testPage.enter("dateOfBirth", "09/14/1950");
        testPage.enter("ssn", "987654321");
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", "Male");
        testPage.enter("livedInMnWholeLife", "Yes");
        testPage.enter("moveToMnDate", "02/18/1950");
        testPage.enter("moveToMnPreviousState", "Illinois");
        testPage.clickContinue();
        testPage.clickButton("Yes, that's everyone");
        testPage.enter("whoNeedsChildCare", "householdMemberFirstName householdMemberLastName");
        testPage.clickContinue();
        testPage.enter("whoHasAParentNotLivingAtHome", "None of the children have parents living outside the home");
        testPage.clickContinue();
        testPage.enter("isPreparingMealsTogether", YES.getDisplayValue());
        testPage.enter("livingSituation", "None of these");
        testPage.clickContinue();
        testPage.enter("goingToSchool", NO.getDisplayValue());
        testPage.enter("isPregnant", YES.getDisplayValue());
        testPage.enter("whoIsPregnant", "Me");
        testPage.clickContinue();
        testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
        testPage.enter("isUsCitizen", NO.getDisplayValue());
        testPage.enter("whoIsNonCitizen", "Me");
        testPage.clickContinue();
        testPage.enter("hasDisability", NO.getDisplayValue());
        testPage.enter("hasWorkSituation", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("areYouWorking", YES.getDisplayValue());
        testPage.clickButton("Add a job");
        testPage.enter("whoseJobIsIt", "householdMemberFirstName householdMemberLastName");
        testPage.clickContinue();
        testPage.enter("employersName", "some employer");
        testPage.clickContinue();
        testPage.enter("selfEmployment", YES.getDisplayValue());
        testPage.enter("paidByTheHour", YES.getDisplayValue());
        testPage.enter("hourlyWage", "1");
        testPage.clickContinue();
        testPage.enter("hoursAWeek", "30");
        testPage.clickContinue();
        testPage.goBack();
        testPage.clickButton("No, I'd rather keep going");
        testPage.clickButton("No, that's it.");
        testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("unearnedIncome", "Social Security");
        testPage.clickContinue();
        testPage.enter("socialSecurityAmount", "200");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "Money from a Trust");
        testPage.clickContinue();
        testPage.enter("trustMoneyAmount", "200");
        testPage.clickContinue();
        testPage.enter("earnLessMoneyThisMonth", "Yes");
        testPage.clickContinue();
        testPage.clickContinue();
        testPage.enter("homeExpenses", "Rent");
        testPage.clickContinue();
        testPage.enter("homeExpensesAmount", "123321");
        testPage.clickContinue();
        testPage.enter("payForUtilities", "Heating");
        testPage.clickContinue();
        testPage.enter("energyAssistance", YES.getDisplayValue());
        testPage.enter("energyAssistanceMoreThan20", YES.getDisplayValue());
        testPage.enter("medicalExpenses", "None of the above");
        testPage.clickContinue();
        testPage.enter("supportAndCare", YES.getDisplayValue());
        testPage.enter("haveVehicle", YES.getDisplayValue());
        testPage.enter("ownRealEstate", YES.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        testPage.enter("haveMillionDollars", NO.getDisplayValue());
        testPage.enter("haveSoldAssets", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("registerToVote", "Yes, send me more info");
        testPage.enter("healthcareCoverage", YES.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("helpWithBenefits", YES.getDisplayValue());
        testPage.enter("communicateOnYourBehalf", YES.getDisplayValue());
        testPage.enter("getMailNotices", YES.getDisplayValue());
        testPage.enter("spendOnYourBehalf", YES.getDisplayValue());
        testPage.enter("helpersFullName", "defaultFirstName defaultLastName");
        testPage.enter("helpersStreetAddress", "someStreetAddress");
        testPage.enter("helpersCity", "someCity");
        testPage.enter("helpersZipCode", "12345");
        testPage.enter("helpersPhoneNumber", "7234567890");
        testPage.clickContinue();
        driver.findElement(By.id("additionalInfo")).sendKeys("Some additional information about my application");
        testPage.clickContinue();
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");
    }

    private static void generateAccessibilityReport(Results results) {
        results.setViolations(resultsList);
        List<Rule> violations = results.getViolations();
        System.out.println("Found " + violations.size() + " accessibility related issues.");
        if (results.getViolations().size() > 0) {
            violations.stream().forEach(violation -> {
                System.out.println("Rule at issue: " + violation.getId());
                System.out.println("Rule description: " + violation.getDescription());
                System.out.println("Rule help text: " + violation.getHelp());
                System.out.println("Rule help page: " + violation.getHelpUrl());
                System.out.println("Accessibility impact: " + violation.getImpact());
                System.out.println("Page at issue: " + violation.getUrl());
                System.out.println("HTML with issue: " + violation.getNodes().get(0).getHtml());
            });
        }
        assertThat(violations.size()).isEqualTo(0);
    }
}
