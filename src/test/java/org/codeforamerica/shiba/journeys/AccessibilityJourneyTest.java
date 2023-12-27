package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.NO;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.testutilities.AccessibilityTestPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;

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
    testPage.clickButton("Upload documents");

    // Enter nothing to throw error on select to check aria-properties on error
    testPage.clickContinue();
    //assertThat(testPage.selectHasInputError("county")).isTrue();
    assertThat(testPage.selectHasInputError("tribalNation")).isTrue();
    assertThat(testPage.getSelectAriaDescribedBy("tribalNation")).isEqualTo("tribalNation-error-message-1");
   // assertThat(testPage.getSelectAriaDescribedBy("county")).isEqualTo("county-error-message-1");

    // should direct me to email docs to my county if my county is not supported
    navigateTo("identifyCounty");
    testPage.enter("county", "Dakota");
    testPage.clickContinue();

    // should allow me to enter personal info and continue the flow if my county is supported
    testPage.clickLink("< Go Back");
    testPage.enter("county", "Hennepin");
    testPage.clickContinue();

    // Enter incorrect information to get validation errors to check against aria-properties
    assertThat(testPage.inputIsValid("firstName")).isTrue();
    assertThat(driver.findElement(By.id("dateOfBirth-day")).getAttribute("aria-invalid")).isEqualTo(
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
    assertThat(driver.findElement(By.id("dateOfBirth-day")).getAttribute("aria-invalid")).isEqualTo(
        "true");
    assertThat(testPage.inputIsValid("ssn")).isFalse();
    assertThat(testPage.getInputAriaLabelledBy("firstName")).isEqualTo(
        "firstName-error-p firstName-label");
    assertThat(testPage.getInputAriaDescribedBy("firstName")).isEqualTo(
        "firstName-error-message-1 firstName-help-message");
    assertThat(driver.findElement(By.id("dateOfBirth-day")).getAttribute("aria-labelledby")).isEqualTo(
        "dateOfBirth-error-p dateOfBirth-legend dateOfBirth-day-label");
    assertThat(
        driver.findElement(By.id("dateOfBirth-day")).getAttribute("aria-describedby")).isEqualTo(
        "dateOfBirth-error-message-1");

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
    // 	howToAddDocuments page is here
    testPage.clickContinue();
    // should allow me to upload documents and those documents should be sent to the ESB
    uploadPdfFile();
    await().until(uploadCompletes());
    testPage.clickButton("Submit my documents");
  }
  
  @Test
  void healthcareRenewalFlow() {
    navigateTo("healthcareRenewalUpload");
    assertThat(driver.getTitle()).isEqualTo("Health Care Renewal Document Upload");

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
    
    // repeat renewal flow to verify another session has been created
    navigateTo("healthcareRenewalUpload");
    assertThat(driver.getTitle()).isEqualTo("Health Care Renewal Document Upload");
    
    WebElement selectedOption = testPage.getSelectedOption("county");
    assertThat(selectedOption.getText()).isEqualTo("Select your county");
    testPage.enter("county", "Hennepin");
    testPage.clickContinue();

    assertThat(driver.getTitle()).isEqualTo("Match Info");
    testPage.enter("firstName", "defaultFirstName");
    testPage.enter("lastName", "defaultLastName");
    testPage.enter("ssn", "123456789");
    testPage.enter("phoneNumber", "7041234567");
    assertThat(testPage.getHeader()).isEqualTo("Before you start, we need to match your documents to your health care case");
    testPage.enter("caseNumber", "12345678");
    testPage.clickContinue();
    testPage.clickContinue();
    navigateTo("healthcareRenewalUpload");
    WebElement selectedOption2 = testPage.getSelectedOption("county");
    assertThat(selectedOption2.getText()).isEqualTo("Hennepin");
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.clickContinue();
    // should allow me to upload documents and those documents should be sent to the ESB
    assertThat(driver.getTitle()).isEqualTo("Upload documents");
    assertThat(driver.findElements(By.className("reveal")).size()).isEqualTo(0);

    uploadPdfFile();
    waitForDocumentUploadToComplete();
    testPage.clickButton("Submit my documents");
    assertThat(driver.getTitle()).isEqualTo("Doc submit confirmation");
    testPage.clickButton("Yes, submit and finish");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");

    // Assert that applicant can't resubmit docs at this point
    navigateTo("uploadDocuments");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");

    navigateTo("documentSubmitConfirmation");
    assertThat(driver.getTitle()).isEqualTo("Documents Sent");
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
    testPage.clickContinue();//for Expedited Notice page

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
    
    fillOutHomeAndMailingAddress("12345", "someCity", "someStreetAddress", "homeApartmentNumber");
            
    // Enter incorrect phone number to throw error and check aria properties
    testPage.enter("phoneNumber", "134567890");
    //testPage.enter("phoneOrEmail", "It's okay to text me");
    testPage.clickContinue();
    assertThat(testPage.hasInputError("phoneNumber")).isTrue();
    assertThat(testPage.getInputAriaLabelledBy("phoneNumber")).isEqualTo(
        "phoneNumber-error-p phoneNumber-label");
    assertThat(testPage.getInputAriaDescribedBy("phoneNumber")).isEqualTo(
        "phoneNumber-error-message-2 phoneNumber-error-message-1 phoneNumber-help-message");
    testPage.enter("phoneNumber", "7234567890");
    testPage.enter("email", "some@example.com");
    testPage.enter("phoneOrEmail", "It's okay to text me");
    testPage.clickContinue();   
    
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
    testPage.selectFromDropdown("selectedTribe[]", "Red Lake Nation");
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
    testPage.clickButton("No, that's it.");
    // drill down to futureIncome page
    testPage.clickButton("No");
    testPage.clickButton("Continue");
    testPage.enter("unearnedIncome", "None of the above");
    testPage.clickButton("Continue");
    testPage.enter("otherUnearnedIncome", "None of the above");
    testPage.clickButton("Continue");
    assertThat(testPage.getInputAriaLabelledBy("div", "earnLessMoneyThisMonth-div")).isEqualTo("page-header page-header-help-message");
    // now back up to jobBuilder page
    testPage.goBack();
    testPage.goBack();
    testPage.goBack();
    testPage.goBack();
    testPage.goBack();
    
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
    driver.navigate().to(baseUrl + "/pages/futureIncome");
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
    testPage.enter("assets", "A vehicle");
    testPage.enter("assets", "Real estate (not including your own home)");
    testPage.clickContinue();
    testPage.enter("haveSavings", YES.getDisplayValue());
    testPage.enter("liquidAssets", "1234");
    testPage.clickContinue();
    testPage.enter("haveSoldAssets", NO.getDisplayValue());
    testPage.clickContinue();
    testPage.clickButton("Yes, send me more info");
    testPage.enter("healthcareCoverage", YES.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("helpWithBenefits", YES.getDisplayValue());
    testPage.enter("communicateOnYourBehalf", YES.getDisplayValue());
    testPage.enter("getMailNotices", YES.getDisplayValue());
    testPage.enter("authorizedRepSpendOnYourBehalf", YES.getDisplayValue());
    testPage.enter("authorizedRepFullName", "defaultFirstName defaultLastName");
    testPage.enter("authorizedRepStreetAddress", "someStreetAddress");
    testPage.enter("authorizedRepCity", "someCity");
    testPage.enter("authorizedRepZipCode", "12345");
    testPage.enter("authorizedRepPhoneNumber", "7234567890");
    testPage.clickContinue();
    driver.findElement(By.id("additionalInfo"))
        .sendKeys("Some additional information about my application");
    testPage.clickContinue();
    testPage.clickLink("Yes, continue");
    testPage.enter("raceAndEthnicity", List.of("Asian", "White"));
    testPage.clickContinue();
    testPage.enter("agreeToTerms", "I agree");
    testPage.enter("drugFelony", NO.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("applicantSignature", "some name");
    testPage.clickButton("Submit");
  }
}
