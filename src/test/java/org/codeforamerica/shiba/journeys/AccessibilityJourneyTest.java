package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.NO;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.mockito.Mockito.when;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.testutilities.AccessibilityTestPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.mock.mockito.MockBean;

@Tag("a11y")
public class AccessibilityJourneyTest extends JourneyTest {

  protected static List<Rule> resultsList = new ArrayList<>();
  protected static Results results;

  @MockBean
  protected DocumentRepository documentRepository;

  @AfterAll
  static void tearDownAll() {
    generateAccessibilityReport(results);
  }

  private static void generateAccessibilityReport(Results results) {
    results.setViolations(resultsList);
    List<Rule> violations = results.getViolations();
    System.out.println("Found " + violations.size() + " accessibility related issues.");
    if (results.getViolations().size() > 0) {
      violations.forEach(violation -> {
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

  @Override
  @BeforeEach
  public void setUp() throws IOException {
    super.setUp();
  }

  protected void initTestPage() {
    testPage = new AccessibilityTestPage(driver);
  }

  @AfterEach
  void afterEach() {
    AxeBuilder builder = new AxeBuilder();
    results = builder.analyze(driver);
    Map<String, List<Rule>> resultMap = ((AccessibilityTestPage) testPage).getResultMap();
    resultMap.values().forEach(resultsList::addAll);
  }

  @Test
  void laterDocsFlow() {
    when(featureFlagConfiguration.get("county-hennepin")).thenReturn(FeatureFlag.ON);
    when(featureFlagConfiguration.get("county-morrison")).thenReturn(FeatureFlag.OFF);
    when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.ON);

    testPage.clickButton("Upload documents");

    // Enter nothing to throw error on select to check aria-properties on error
    testPage.clickContinue();
    assertThat(testPage.selectHasInputError("county")).isTrue();
    assertThat(testPage.getSelectAriaLabel("county")).isEqualTo("Error county");
    assertThat(testPage.getSelectAriaDescribedBy("county")).isEqualTo("county-error-message-1");
    testPage.clickLink("Enter my zip code instead.");

    // Check that the input is valid, then intentially throw an error and check it is invalid
    assertThat(testPage.inputIsValid("zipCode")).isTrue();
    testPage.enter("zipCode", "11");
    testPage.clickContinue();
    assertThat(testPage.hasInputError("zipCode")).isTrue();
    assertThat(testPage.inputIsValid("zipCode")).isFalse();
    assertThat(testPage.getInputAriaLabel("zipCode")).isEqualTo("Error zipCode");
    assertThat(testPage.getInputAriaDescribedBy("zipCode")).isEqualTo("zipCode-error-message-1");
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

    // Enter incorrect information to get validation errors to check against aria-properties
    assertThat(testPage.inputIsValid("firstName")).isTrue();
    assertThat(driver.findElementById("dateOfBirth-day").getAttribute("aria-invalid")).isEqualTo(
        "false");
    assertThat(testPage.inputIsValid("ssn")).isTrue();
    testPage.enter("firstName", "");
    testPage.enter("lastName", "defaultLastName");
    testPage.enter("dateOfBirth", "01/40/1999");
    testPage.enter("ssn", "12345");
    testPage.enter("caseNumber", "1234567");
    testPage.clickContinue();
    assertThat(testPage.hasInputError("firstName")).isTrue();
    assertThat(testPage.hasInputError("ssn")).isTrue();
    assertThat(testPage.inputIsValid("firstName")).isFalse();
    assertThat(driver.findElementById("dateOfBirth-day").getAttribute("aria-invalid")).isEqualTo(
        "true");
    assertThat(testPage.inputIsValid("ssn")).isFalse();
    assertThat(testPage.getInputAriaLabelledBy("firstName")).isEqualTo(
        "firstName-error-p firstName-label");
    assertThat(testPage.getInputAriaDescribedBy("firstName")).isEqualTo(
        "firstName-error-message-1 firstName-help-message");
    assertThat(driver.findElementById("dateOfBirth-day").getAttribute("aria-labelledby")).isEqualTo(
        "dateOfBirth-error-p dateOfBirth-legend dateOfBirth-day-label");
    assertThat(
        driver.findElementById("dateOfBirth-day").getAttribute("aria-describedby")).isEqualTo(
        "dateOfBirth-error-message-1");
    ;
    assertThat(testPage.getInputAriaLabelledBy("ssn")).isEqualTo("ssn-error-p ssn-label");
    assertThat(testPage.getInputAriaDescribedBy("ssn")).isEqualTo(
        "ssn-error-message-1 ssn-help-message");

    testPage.enter("firstName", "defaultFirstName");
    testPage.enter("lastName", "defaultLastName");
    testPage.enter("dateOfBirth", "01/12/1928");
    testPage.enter("ssn", "123456789");
    testPage.enter("caseNumber", "1234567");
    testPage.enter("phoneNumber", "7041234567");
    testPage.clickContinue();

    // should allow me to upload documents and those documents should be sent to the ESB
    uploadPdfFile();
    await().until(uploadCompletes());
    testPage.clickButton("Submit my documents");
  }

  @Test
  void userCanCompleteTheNonExpeditedHouseholdFlow() {
    List<String> programSelections = List.of(PROGRAM_SNAP, PROGRAM_CCAP);

    testPage.clickButton("Apply now");
    testPage.enter("county", "Hennepin");
    testPage.clickContinue();

    testPage.clickContinue();
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

    // Enter incorrect phone number to throw error and check aria properties
    testPage.enter("phoneNumber", "134567890");
    //testPage.enter("phoneOrEmail", "It's okay to text me");
    testPage.clickContinue();
    assertThat(testPage.hasInputError("phoneNumber")).isTrue();
    assertThat(testPage.getInputAriaLabelledBy("phoneNumber")).isEqualTo(
        "phoneNumber-error-p phoneNumber-label");
    assertThat(testPage.getInputAriaDescribedBy("phoneNumber")).isEqualTo(
        "phoneNumber-error-message-2 phoneNumber-error-message-1");

    testPage.enter("phoneNumber", "7234567890");
    testPage.enter("email", "some@example.com");
    testPage.enter("phoneOrEmail", "It's okay to text me");
    testPage.clickContinue();
    fillOutHomeAndMailingAddress("12345", "someCity", "someStreetAddress", "homeApartmentNumber");
    testPage.clickLink("This looks correct");
    testPage.enter("addHouseholdMembers", YES.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("relationship", "Other");
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
    testPage.enter("whoHasAParentNotLivingAtHome",
        "None of the children have parents living outside the home");
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
    testPage.enter("isTribalNationMember", YES.getDisplayValue());
    testPage.selectFromDropdown("selectedTribe[]", "Red Lake");
    testPage.clickContinue();
    testPage.enter("livingInNationBoundary", NO.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("areYouWorking", YES.getDisplayValue());
    testPage.clickButton("Add a job");
    testPage.enter("whoseJobIsIt", "householdMemberFirstName householdMemberLastName");
    testPage.clickContinue();

    // Leave black to trigger error and check aria properties
    assertThat(testPage.inputIsValid("employersName")).isTrue();
    assertThat(testPage.getInputAriaLabelledBy("employersName")).isEqualTo("employersName-label");
    testPage.clickContinue();
    assertThat(testPage.inputIsValid("employersName")).isFalse();
    assertThat(testPage.getInputAriaLabelledBy("employersName")).isEqualTo(
        "employersName-error-p employersName-label");
    assertThat(testPage.getInputAriaDescribedBy("employersName")).isEqualTo(
        "employersName-error-message-1");

    testPage.enter("employersName", "some employer");
    testPage.clickContinue();
    testPage.enter("selfEmployment", YES.getDisplayValue());
    testPage.enter("paidByTheHour", YES.getDisplayValue());

    // Check aria-label is correct then enter incorrect value to throw error and check all aria properties have updated
    testPage.enter("hourlyWage", "-10");
    testPage.clickContinue();
    assertThat(testPage.inputIsValid("hourlyWage")).isFalse();
    assertThat(testPage.getInputAriaLabel("hourlyWage")).isEqualTo("Error hourlyWage");
    assertThat(testPage.getInputAriaDescribedBy("hourlyWage")).isEqualTo(
        "hourlyWage-error-message-1");

    testPage.enter("hourlyWage", "1");
    testPage.clickContinue();

    // Enter an incorrect value to trigger an error and check aria properties
    assertThat(testPage.inputIsValid("hoursAWeek")).isTrue();
    testPage.enter("hoursAWeek", "-30");
    testPage.clickContinue();
    assertThat(testPage.inputIsValid("hoursAWeek")).isFalse();
    assertThat(testPage.getInputAriaLabel("hoursAWeek")).isEqualTo("Error hoursAWeek");
    assertThat(testPage.getInputAriaDescribedBy("hoursAWeek")).isEqualTo(
        "hoursAWeek-error-message-1");

    testPage.enter("hoursAWeek", "30");
    testPage.clickContinue();
    testPage.goBack();
    testPage.clickButton("No, I'd rather keep going");
    testPage.clickButton("No, that's it.");
    testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("unearnedIncome", "Social Security");
    testPage.clickContinue();

    // Enter incorrect social security amount to trigger error and check aria properties
    testPage.enter("socialSecurityAmount", "-200");
    testPage.clickContinue();
    testPage.hasInputError("socialSecurityAmount");
    assertThat(testPage.inputIsValid("socialSecurityAmount")).isFalse();
    assertThat(testPage.getInputAriaDescribedBy("socialSecurityAmount")).isEqualTo(
        "socialSecurityAmount-error-message-1 socialSecurityAmount-help-message");
    assertThat(testPage.getInputAriaLabelledBy("socialSecurityAmount")).isEqualTo(
        "socialSecurityAmount-error-p socialSecurityAmount-label");

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
    driver.findElement(By.id("additionalInfo"))
        .sendKeys("Some additional information about my application");
    testPage.clickContinue();
    testPage.enter("agreeToTerms", "I agree");
    testPage.enter("drugFelony", NO.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("applicantSignature", "some name");
    testPage.clickButton("Submit");
  }
}
