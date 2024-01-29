package org.codeforamerica.shiba.journeys;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.application.FlowType.FULL;
import static org.codeforamerica.shiba.testutilities.TestUtils.getAbsoluteFilepathString;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.NO;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.testutilities.PercyTestPage;
import org.codeforamerica.shiba.testutilities.SuccessPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@Tag("fullFlowJourney")
public class FullFlowJourneyTest extends JourneyTest {

  protected void initTestPage() {
    testPage = new PercyTestPage(driver);
  }

  @Test
  void fullApplicationWithDocumentUploads() {
    when(clock.instant()).thenReturn(
        LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
        LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
    );
    when(featureFlagConfiguration.get("certain-pops")).thenReturn(FeatureFlag.ON);

    // Assert intercom button is present on landing page
    await().atMost(5, SECONDS).until(() -> !driver.findElements(By.id("intercom-frame")).isEmpty());
    assertThat(driver.findElement(By.id("intercom-frame"))).isNotNull();
    // Assert that the Delayed Processing Time Notice is displayed on the landing page.
    assertThat(driver.findElement(By.id("delayed-processing-time-notice"))).isNotNull();
    // Assert that the EBT Scam Alert is displayed on the landing page.
    assertThat(driver.findElement(By.id("ebt-scam-alert"))).isNotNull();
   
    // Verify that the "Learn more here." link works 
    String landingPageWindowHandle = driver.getWindowHandle();
    testPage.clickLink("Learn more here.");
    ArrayList<String> windowHandles = new ArrayList<String>(driver.getWindowHandles());
    driver.switchTo().window(windowHandles.get(1));
    assertThat(driver.getTitle().equals("Recent reports of card skimming affecting EBT card users"));
    driver.switchTo().window(landingPageWindowHandle);
    
    // Assert presence and functionality of the SNAP non-discrimination link on the footer.
    assertThat(driver.findElement(By.id("link-snap-nds"))).isNotNull();
    
    goToPageBeforeSelectPrograms("Chisago");
     
    selectProgramsWithoutCertainPopsAndEnterPersonalInfo();
    fillOutHomeAndMailingAddressWithoutEnrich("12345", "someCity", "someStreetAddress", "someApartmentNumber");
    
    fillOutHomeAndMailingAddress("12345", "someCity", "someStreetAddress", "someApartmentNumber");
    fillOutContactAndReview(true, "Chisago");
    
    testPage.clickLink("This looks correct");
    verifyHouseholdMemberCannotSelectCertainPops();
    goBackToPage("Choose Programs");
    
    selectAllProgramsAndVerifyApplicantIsQualifiedForCertainPops();

    fillOutHomeAndMailingAddress("12345", "someCity", "someStreetAddress", "someApartmentNumber");
    goToContactAndReview();
 
    addSpouseAndVerifySpouseCanSelectCertainPops();

  	addHouseholdMemberToVerifySpouseCannotBeSelected();
    removeSpouseAndVerifySpouseCanBeSelectedForNewHouseholdMember();
 
    String householdMemberFirstName = "householdMemberFirstName";
    String householdMemberLastName = "householdMemberLastName";
    String householdMemberFullName = householdMemberFirstName + " " + householdMemberLastName;
    
    testPage.clickButton("Yes, that's everyone");
    
    // Who are the children in need of childcare
    testPage.enter("whoNeedsChildCare", householdMemberFullName);
    testPage.clickContinue();

    // Who are the children that have a parent not living at home?
    testPage.enter("whoHasAParentNotLivingAtHome", householdMemberFullName);
    testPage.clickContinue();

    // Tell us the name of any parent living outside the home.
    String parentNotAtHomeName = "My child's parent";
    driver.findElement(By.name("whatAreTheParentsNames[]")).sendKeys(parentNotAtHomeName);
    testPage.clickContinue();

    // Does everyone in your household buy and prepare food with you?
    testPage.enter("isPreparingMealsTogether", YES.getDisplayValue());
    
    // Are you getting a housing subsidy?
    testPage.enter("hasHousingSubsidy", YES.getDisplayValue());

    // What is your current living situation?
    testPage.enter("livingSituation", "Staying in a hotel or motel");
    testPage.clickContinue();

    // Is anyone in your household going to school right now, either full or part-time?
    testPage.enter("goingToSchool", YES.getDisplayValue());

    // Who is going to school?
    testPage.enter("whoIsGoingToSchool", List.of(householdMemberFullName));
    testPage.clickContinue();

    // Is anyone in your household pregnant?
    testPage.enter("isPregnant", YES.getDisplayValue());

    // Who is pregnant?
    testPage.enter("whoIsPregnant", "me");
    testPage.clickContinue();

    // Is anyone in your household a migrant or seasonal farm worker?
    testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());

    // Is everyone in your household a U.S. Citizen?
    testPage.enter("isUsCitizen", NO.getDisplayValue());

    // Who is not a U.S Citizen?
    testPage.enter("whoIsNonCitizen", "me");
    testPage.clickContinue();
    
    testPage.enter("alienIdNumber", "A12345678");
    testPage.clickContinue();

    // Does anyone in your household have a physical or mental disability that prevents them from working?
    testPage.enter("hasDisability", YES.getDisplayValue());
    
    //Who has Disability?
    testPage.enter("whoHasDisability", "me");
    testPage.clickContinue();
    
    // In the last 2 months, did anyone in your household do any of these things?
    testPage.enter("hasWorkSituation", NO.getDisplayValue());

    // Is anyone in your household a member of a tribal nation?
    testPage.enter("isTribalNationMember", YES.getDisplayValue());

    // Select MFIP
    testPage.selectFromDropdown("selectedTribe[]", "Prairie Island");
    testPage.clickContinue();

    // Are any of the tribal members in your household living in or near the nation’s boundaries?
    testPage.enter("livingInNationBoundary", YES.getDisplayValue());

    // It looks like you might be eligible for MFIP. Would you like to apply?
    // This triggers CASH to be mapped for CAF program selections
    testPage.enter("applyForMFIP", YES.getDisplayValue());

    // MFIP Confirmation screen
    testPage.clickContinue();

    navigateTo("selectTheTribe");
    // Go back and select a tribe that routes to Mille Lacs
    testPage.selectFromDropdown("selectedTribe[]", "Bois Forte");
    testPage.clickContinue();

    // It looks like you might be eligible for Tribal TANF. Would you like to apply?
    // This triggers Tribal TANF to be mapped for CAF program selections
    testPage.enter("applyForTribalTANF", YES.getDisplayValue());

    // Tribal TANF Confirmation screen
    testPage.clickContinue();

    // Medical Care Milestone
    testPage.clickContinue();

    // Long Term Care
    testPage.enter("doYouNeedLongTermCare", YES.getDisplayValue());

    //Past Injury
    testPage.enter("didYouHaveAPastInjury", YES.getDisplayValue());

    //Retroactive Coverage
    testPage.enter("retroactiveCoverageQuestion", YES.getDisplayValue());
    
    //Retroactive Coverage Source
    testPage.enter("retroactiveCoverageSourceQuestion", "me");
    testPage.clickContinue();
    
    //Retroactive Coverage Time Period
    testPage.selectFromDropdown("retroactiveCoverageNumberMonths[]", "1 month");
    testPage.clickContinue();

    //Medical Benefits in another state
    testPage.enter("medicalInOtherState", YES.getDisplayValue());

    // Income & Employment
    // Certain Pops will increment milestone steps
    assertThat(testPage.getElementText("milestone-step")).isEqualTo("Step 4 of 7");
    testPage.clickContinue();

    // Is anyone in your household making money from a job?
    testPage.enter("areYouWorking", YES.getDisplayValue());

    // Add a job for the household
    testPage.clickButton("Add a job");
    testPage.enter("whoseJobIsIt", householdMemberFullName);
    testPage.clickContinue();
    testPage.enter("employersName", "some employer");
    testPage.clickContinue();
    testPage.enter("selfEmployment", YES.getDisplayValue());
    testPage.enter("paidByTheHour", YES.getDisplayValue());
    testPage.enter("hourlyWage", "1.00");
    testPage.clickContinue();
    testPage.enter("hoursAWeek", "30");
    testPage.clickContinue();
    testPage.goBack();
    testPage.clickButton("No, I'd rather keep going");

    // Add a second job and delete
    testPage.clickButton("Add a job");
    testPage.enter("whoseJobIsIt", householdMemberFullName);
    testPage.clickContinue();
    testPage.enter("employersName", "some employer");
    testPage.clickContinue();
    testPage.enter("selfEmployment", YES.getDisplayValue());
    testPage.enter("paidByTheHour", YES.getDisplayValue());
    testPage.enter("hourlyWage", "1.00");
    testPage.clickContinue();
    testPage.enter("hoursAWeek", "30");
    testPage.clickContinue();

    // You are about to delete your job
    driver.findElement(By.id("iteration1-delete")).click();
    testPage.clickButton("Yes, remove the job");

    testPage.clickButton("No, that's it.");

    // Is anyone in the household currently looking for a job?
    testPage.enter("currentlyLookingForJob", NO.getDisplayValue());

    // Got it! You're almost done with the income section.
    testPage.clickContinue();

    // Does anyone in your household get income from these sources?
    testPage.enter("unearnedIncome", "Social Security");
    testPage.clickContinue();

    // Tell us how much money is received.
    testPage.clickElementById("householdMember-me");
    testPage.enter("socialSecurityAmount", "200.30");
    testPage.clickContinue();

    // Does anyone in your household get income from these other sources?
    testPage.enter("otherUnearnedIncome", "Other Minnesota Benefits Programs (Benefits like GA, MFIP, Tribal TANF or others)");
    testPage.enter("otherUnearnedIncome", "Insurance Payments");
    testPage.enter("otherUnearnedIncome", "Contract for Deed");
    testPage.enter("otherUnearnedIncome", "Money from a Trust");
    testPage.enter("otherUnearnedIncome", "Rental Income"); // Only Certain Pops
    testPage.enter("otherUnearnedIncome", "Health Care Reimbursement");
    testPage.enter("otherUnearnedIncome", "Interest / Dividends");
    testPage.enter("otherUnearnedIncome", "Other payments");
    testPage.clickContinue();

    // Choose who receives that income (CCAP and CERTAIN_POPS only)
    testPage.clickElementById("householdMember-me");
    testPage.enter("insurancePaymentsAmount", "100.00");
    testPage.clickContinue();
    testPage.clickElementById("householdMember-me");
    testPage.enter("trustMoneyAmount", "100.00");
    testPage.clickContinue();
    testPage.clickElementById("householdMember-me");
    testPage.enter("rentalIncomeAmount", "100.00");
    testPage.clickContinue();
    testPage.clickElementById("householdMember-me");
    testPage.enter("interestDividendsAmount", "100.00");
    testPage.clickContinue();
    testPage.clickElementById("householdMember-me");
    testPage.enter("healthCareReimbursementAmount", "100.00");
    testPage.clickContinue();
    testPage.clickElementById("householdMember-me");
    testPage.enter("benefitsAmount", "100.00");
    testPage.clickContinue();
    testPage.clickElementById("householdMember-me");
    testPage.enter("contractForDeedAmount", "100.00");
    testPage.clickContinue();
    testPage.clickElementById("householdMember-me");
    testPage.enter("otherPaymentsAmount", "100.00");
    testPage.clickContinue();

    // Do you think the household will earn less money this month than last month?
    testPage.enter("earnLessMoneyThisMonth", "Yes");
    driver.findElement(By.id("additionalIncomeInfo"))
        .sendKeys("I also make a small amount of money from my lemonade stand.");
    testPage.clickContinue();

    // Expenses & Deductions
    testPage.clickContinue();

    // Does anyone in your household pay for room and board?
    testPage.enter("homeExpenses", "Room and Board");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Home expenses amount");
    // Make sure the header says room and board
    assertThat(testPage.getHeader())
        .isEqualTo("How much does your household pay for room and board every month?");

    testPage.goBack();

    // Does anyone in your household pay for any of these?
    testPage.enter("homeExpenses", "Rent");
    testPage.enter("homeExpenses", "Mortgage");
    testPage.clickContinue();
    // Make sure the header includes all three selections
    assertThat(testPage.getHeader()).isEqualTo(
        "How much does your household pay for rent, mortgage and room and board every month?");

    // How much does your household pay for your rent and mortgage every month?
    testPage.enter("homeExpensesAmount", "123321.50");
    testPage.clickContinue();

    // Does anyone in your household pay for utilities?
    testPage.enter("payForUtilities", "Heating");
    testPage.clickContinue();

    // Has your household received money for energy assistance (LIHEAP) in the last 12 months?
    testPage.enter("energyAssistance", YES.getDisplayValue());

    // Has your household received more than $20 in energy assistance this year?
    testPage.enter("energyAssistanceMoreThan20", YES.getDisplayValue());
    testPage.enter("medicalExpenses", "Dental insurance premiums");
    testPage.enter("medicalExpenses", "Vision insurance premiums");
    testPage.enter("medicalExpenses", "Medical insurance premiums");
    testPage.clickContinue();

    // Tell us how much money is paid.
    testPage.enter("dentalInsurancePremiumAmount", "12.34");
    testPage.enter("visionInsurancePremiumAmount", "56.35");
    testPage.enter("medicalInsurancePremiumAmount", "10.90");

    testPage.clickContinue();

    // Does anyone in the household pay for court-ordered child support, spousal support, child care support or medical care?
    testPage.enter("supportAndCare", YES.getDisplayValue());

    // Does anyone in your household have any of these?
    testPage.enter("assets", "A vehicle");
    testPage.enter("assets", "Stocks, bonds, retirement accounts");
    testPage.enter("assets", "Real estate (not including your own home)");
    testPage.clickContinue();

    // Who has a vehicle?
    assertThat(testPage.getTitle()).isEqualTo("Who has a vehicle");
    driver.findElement(By.id("householdMember-me")).click();  
    testPage.clickContinue();
    
    //Which types of investment accounts does your household have? 
    assertThat(testPage.getTitle()).isEqualTo("Which types of investment accounts does your household have");
    driver.findElement(By.id("STOCKS")).click(); 
    testPage.clickContinue();
    
    //
    assertThat(testPage.getTitle()).isEqualTo("Who has stocks");
    driver.findElement(By.id("householdMember-me")).click(); 
    testPage.clickContinue();
    
    
    //Who has real estate (not including your own home)
    assertThat(testPage.getTitle()).isEqualTo("Who has real estate (not including your own home)");
    driver.findElement(By.id("householdMember-me")).click();  
    testPage.clickContinue();
    
    // Does anyone in the household have money in a bank account or debit card?
    testPage.enter("haveSavings", YES.getDisplayValue());

    // How much cash does your household have available?
    testPage.enter("cashAmount", "1234");
    testPage.clickContinue();
    
    //Does your household have any of these accounts?
    driver.findElement(By.id("SAVINGS")).click();
    testPage.clickContinue();
    
    //who has money in a savings account?
    driver.findElement(By.id("householdMember-me")).click();    
    testPage.clickContinue();
    
    //how much money is available in these accounts?
    testPage.enter("liquidAssets", "1234");
    testPage.clickContinue();
    
    // In the last 12 months, has anyone in the household given away or sold any assets?
    testPage.enter("haveSoldAssets", NO.getDisplayValue());

    // Submitting your Application
    testPage.clickContinue();
    testPage.clickButton("Yes, send me more info");

    // Do you currently have healthcare coverage?
    testPage.enter("healthcareCoverage", YES.getDisplayValue());
    testPage.clickContinue();

    // Do you want to assign someone to help with your benefits?
    testPage.enter("helpWithBenefits", YES.getDisplayValue());

    // Do you want your helper to communicate with the county on your behalf?
    testPage.enter("communicateOnYourBehalf", YES.getDisplayValue());

    // Do you want your helper to get mail and notices for you?
    testPage.enter("getMailNotices", YES.getDisplayValue());

    // Do you want your helper to spend your benefits on your behalf?
    testPage.enter("authorizedRepSpendOnYourBehalf", YES.getDisplayValue());

    // Let's get your helpers contact information
    testPage.enter("authorizedRepFullName", "defaultFirstName defaultLastName");
    testPage.enter("authorizedRepStreetAddress", "someStreetAddress");
    testPage.enter("authorizedRepCity", "someCity");
    testPage.enter("authorizedRepZipCode", "12345");
    testPage.enter("authorizedRepPhoneNumber", "7234567890");
    testPage.clickContinue();

    // Is there anything else you want to share?
    driver.findElement(By.id("additionalInfo"))
        .sendKeys("I need you to contact my work for proof of termination");
    testPage.clickContinue();

    // Can we ask about your race and ethnicity?
    testPage.clickLink("Yes, continue");

    // What races or ethnicities do you identify with?
    testPage.enter("raceAndEthnicity", List.of("Black or African American"));
    testPage.clickContinue();

    // The legal stuff.
    testPage.enter("agreeToTerms", "I agree");
    testPage.enter("drugFelony", NO.getDisplayValue());
    testPage.clickContinue();

    // Upload documents
    testPage.enter("applicantSignature", "this is my signature");
    testPage.clickButton("Continue");
    testPage.clickButton("Submit");
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.clickButton("Add documents now");
    testPage.clickContinue();

    testDocumentUploads();

    // Finish uploading docs, view next steps, and download PDFs
    testPage.clickButton("Submit my documents");
    testPage.clickButton("Yes, submit and finish");

    // Assert that applicant can't resubmit docs at this point
    navigateTo("uploadDocuments");
    assertThat(driver.getTitle()).isEqualTo("Your next steps");
    
    // TODO:  Fix this conditional logic once the enhanced nextSteps page is fully implemented.
    List<WebElement> pageElements = driver.findElements(By.id("original-next-steps"));
    testPage.clickElementById("button-a2");
    testPage.clickElementById("button-a3");
    testPage.clickElementById("button-a4");
    if (pageElements.isEmpty()) {
    	List<String> expectedMessages = List.of(
        		"We received the documents you uploaded with your application.",
        		"If you need to upload more documents later, you can return to our homepage and click on ‘Upload documents’ to get started.",
        		"Within the next 5 days, expect a phone call from an eligibility worker with information about your next steps.",
        		"Program(s) on your application may require you to talk with a worker about your application.",
        		"A worker from your county or Tribal Nation will call you. If they can\u2019t reach you, they will send you a letter.");
    	List<String> nextStepSections = driver.findElements(By.className("next-step-section")).stream().map(WebElement::getText).collect(Collectors.toList());
    	assertThat(nextStepSections).containsExactly(expectedMessages.toArray(new String[0]));
    }

    navigateTo("documentSubmitConfirmation");
    assertThat(driver.getTitle()).isEqualTo("Your next steps");
    // Assert that the Delayed Processing Time Notice is displayed on the nextSteps page.
    assertThat(driver.findElement(By.id("delayed-processing-time-notice"))).isNotNull();
    testPage.clickContinue();

    SuccessPage successPage = new SuccessPage(driver);
    assertThat(successPage.findElementById("submission-date").getText()).contains(
        "Your application was submitted to Chisago County (888-234-1246) and Mille Lacs Band of Ojibwe (320-532-7407) on January 1, 2020.");
    applicationId = downloadPdfs();
    assertThat(successPage.findElementById("confirmation-number").getText()).contains("Confirmation # " + applicationId);

    // CCAP fields
    assertCcapFieldEquals("APPLICATION_ID", applicationId);
    assertCcapFieldEquals("SUBMISSION_DATETIME", "01/01/2020 at 04:15 AM");
    assertCcapFieldEquals("PAY_FREQUENCY_0", "Hourly");
    assertCcapFieldEquals("EMPLOYEE_FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("DATE_OF_BIRTH", "01/12/1928");
    assertCcapFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
    assertCcapFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
    assertCcapFieldEquals("APPLICANT_EMAIL", "some@example.com");
    assertCcapFieldEquals("PHONE_OPTIN", "Yes");
    assertCcapFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", "");
    assertCcapFieldEquals("EMPLOYERS_NAME_0", "some employer");
    assertCcapFieldEquals("INCOME_PER_PAY_PERIOD_0", "1.00");
    assertCcapFieldEquals("DATE_OF_BIRTH_0", "09/14/2018");
    assertCcapFieldEquals("SSN_0", "XXX-XX-XXXX");
    assertCcapFieldEquals("COUNTY_INSTRUCTIONS", """
        This application was submitted to Chisago County with the information that you provided. Some parts of this application will be blank. A caseworker will follow up with you if additional information is needed.
                    
        For more support, you can call Chisago County (888-234-1246).""");
    assertCcapFieldEquals("PROGRAMS", "SNAP, CCAP, EA, GRH, CERTAIN_POPS, TRIBAL TANF, CASH");
    assertCcapFieldEquals("FULL_NAME", "Ahmed St. George");
    assertCcapFieldEquals("UTM_SOURCE", "");
    assertCcapFieldEquals("FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("TRIBAL_NATION", "Bois Forte");
    assertCcapFieldEquals("PROGRAMS_0", "CCAP, CERTAIN_POPS");
    assertCcapFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "SNAP");
    assertCcapFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "CCAP");
    assertCcapFieldEquals("GROSS_MONTHLY_INCOME_0", "120.00");
    assertCcapFieldEquals("APPLICANT_MAILING_ZIPCODE", "03104");
    assertCcapFieldEquals("APPLICANT_MAILING_CITY", "Cooltown");
    assertCcapFieldEquals("APPLICANT_MAILING_STATE", "CA");
    assertCcapFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", "smarty street");
    assertCcapFieldEquals("APPLICANT_HOME_CITY", "someCity");
    assertCcapFieldEquals("APPLICANT_HOME_STATE", "MN");
    assertCcapFieldEquals("APPLICANT_HOME_ZIPCODE", "12345");
    assertCcapFieldEquals("LIVING_SITUATION", "HOTEL_OR_MOTEL");
    assertCcapFieldEquals("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE", "ENGLISH");
    assertCcapFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH");
    assertCcapFieldEquals("NEED_INTERPRETER", "Yes");
    assertCcapFieldEquals("APPLICANT_FIRST_NAME", "Ahmed");
    assertCcapFieldEquals("APPLICANT_LAST_NAME", "St. George");
    assertCcapFieldEquals("APPLICANT_OTHER_NAME", "defaultOtherName");
    assertCcapFieldEquals("DATE_OF_BIRTH", "01/12/1928");
    assertCcapFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
    assertCcapFieldEquals("MARITAL_STATUS", "NEVER_MARRIED");
    assertCcapFieldEquals("APPLICANT_SEX", "FEMALE");
    assertCcapFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
    assertCcapFieldEquals("APPLICANT_EMAIL", "some@example.com");
    assertCcapFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "someStreetAddress");
    assertCcapFieldEquals("ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0", "");
    assertCcapFieldEquals("ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_0", "");
    assertCcapFieldEquals("STUDENT_FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("SSI", "No");
    assertCcapFieldEquals("VETERANS_BENEFITS", "No");
    assertCcapFieldEquals("UNEMPLOYMENT", "No");
    assertCcapFieldEquals("WORKERS_COMPENSATION", "No");
    assertCcapFieldEquals("RETIREMENT", "No");
    assertCcapFieldEquals("CHILD_OR_SPOUSAL_SUPPORT", "No");
    assertCcapFieldEquals("TRIBAL_PAYMENTS", "No");
    assertCcapFieldEquals("SELF_EMPLOYMENT_EMPLOYEE_FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("IS_US_CITIZEN_0", "Yes");
    assertCcapFieldEquals("SOCIAL_SECURITY_FREQUENCY", "Monthly");
    assertCcapFieldEquals("MEDICAL_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
    assertCcapFieldEquals("VISION_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
    assertCcapFieldEquals("DENTAL_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
    assertCcapFieldEquals("MEDICAL_INSURANCE_PREMIUM_AMOUNT", "10.90");
    assertCcapFieldEquals("DENTAL_INSURANCE_PREMIUM_AMOUNT", "12.34");
    assertCcapFieldEquals("VISION_INSURANCE_PREMIUM_AMOUNT", "56.35");
    assertCcapFieldEquals("IS_WORKING", "No");
    assertCcapFieldEquals("SOCIAL_SECURITY", "Yes");
    assertCcapFieldEquals("TRUST_MONEY", "Yes");
    assertCcapFieldEquals("BENEFITS", "Yes");
    assertCcapFieldEquals("INSURANCE_PAYMENTS", "Yes");
    assertCcapFieldEquals("CONTRACT_FOR_DEED", "Yes");
    assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT", "Yes");
    assertCcapFieldEquals("INTEREST_DIVIDENDS", "Yes");
    assertCcapFieldEquals("OTHER_PAYMENTS", "Yes");
    assertCcapFieldEquals("TRUST_MONEY_AMOUNT", "100.00");
    assertCcapFieldEquals("SOCIAL_SECURITY_AMOUNT", "200.30");
    assertCcapFieldEquals("BENEFITS_AMOUNT", "100.00");
    assertCcapFieldEquals("INSURANCE_PAYMENTS_AMOUNT", "100.00");
    assertCcapFieldEquals("CONTRACT_FOR_DEED_AMOUNT", "100.00");
    assertCcapFieldEquals("TRUST_MONEY_AMOUNT", "100.00");
    assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT_AMOUNT", "100.00");
    assertCcapFieldEquals("INTEREST_DIVIDENDS_AMOUNT", "100.00");
    assertCcapFieldEquals("OTHER_PAYMENTS_AMOUNT", "100.00");
    assertCcapFieldEquals("BENEFITS_FREQUENCY", "Monthly");
    assertCcapFieldEquals("INSURANCE_PAYMENTS_FREQUENCY", "Monthly");
    assertCcapFieldEquals("CONTRACT_FOR_DEED_FREQUENCY", "Monthly");
    assertCcapFieldEquals("TRUST_MONEY_FREQUENCY", "Monthly");
    assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT_FREQUENCY", "Monthly");
    assertCcapFieldEquals("INTEREST_DIVIDENDS_FREQUENCY", "Monthly");
    assertCcapFieldEquals("OTHER_PAYMENTS_FREQUENCY", "Monthly");
    assertCcapFieldEquals("EARN_LESS_MONEY_THIS_MONTH", "Yes");
    assertCcapFieldEquals("ADDITIONAL_INCOME_INFO",
        "I also make a small amount of money from my lemonade stand.");
    assertCcapFieldEquals("HAVE_MILLION_DOLLARS", "No");
    assertCcapFieldEquals("PARENT_NOT_LIVING_AT_HOME_0", "My child's parent");
    assertCcapFieldEquals("CHILD_FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("SELF_EMPLOYMENT_HOURS_A_WEEK_0", "30");
    assertCcapFieldEquals("LAST_NAME_0", "householdMemberLastName");
    assertCcapFieldEquals("SEX_0", "MALE");
    assertCcapFieldEquals("DATE_OF_BIRTH_0", "09/14/2018");
    assertCcapFieldEquals("SSN_0", "XXX-XX-XXXX");
    assertCcapFieldEquals("FIRST_NAME_0", "householdMemberFirstName");
    assertCcapFieldEquals("RELATIONSHIP_0", "child");
    assertCcapFieldEquals("SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0", "120.00");
    assertCcapFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "Off");
    assertCcapFieldEquals("CREATED_DATE", "2020-01-01");
    assertCcapFieldEquals("APPLICANT_SIGNATURE", "this is my signature");
    assertCcapFieldEquals("ADDITIONAL_APPLICATION_INFO",
        "I need you to contact my work for proof of termination");
    assertCcapFieldEquals("BLACK_OR_AFRICAN_AMERICAN", "Yes");
    assertCcapFieldEquals("HISPANIC_LATINO_OR_SPANISH_NO", "Yes");
    assertCcapFieldEquals("REGISTER_TO_VOTE", "Yes");

    // CAF
    assertCafFieldEquals("APPLICATION_ID", applicationId);
    assertCafFieldEquals("SUBMISSION_DATETIME", "01/01/2020 at 04:15 AM");
    assertCafFieldEquals("PAY_FREQUENCY_0", "Hourly");
    assertCafFieldEquals("EMPLOYEE_FULL_NAME_0", householdMemberFullName);
    assertCafFieldEquals("DATE_OF_BIRTH", "01/12/1928");
    assertCafFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
    assertCafFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
    assertCafFieldEquals("APPLICANT_EMAIL", "some@example.com");
    assertCafFieldEquals("PHONE_OPTIN", "Yes");
    assertCafFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", "");
    assertCafFieldEquals("EMPLOYERS_NAME_0", "some employer");
    assertCafFieldEquals("INCOME_PER_PAY_PERIOD_0", "1.00");
    assertCafFieldEquals("DATE_OF_BIRTH_0", "09/14/2018");
    assertCafFieldEquals("SSN_0", "XXX-XX-XXXX");
    assertCafFieldEquals("COUNTY_INSTRUCTIONS",
        """
            This application was submitted to Mille Lacs Band of Ojibwe and Chisago County with the information that you provided. Some parts of this application will be blank. A caseworker will follow up with you if additional information is needed.

            For more support, you can call Mille Lacs Band of Ojibwe (320-532-7407) and Chisago County (888-234-1246).""");
    assertCafFieldEquals("PROGRAMS", "SNAP, CCAP, EA, GRH, CERTAIN_POPS, TRIBAL TANF, CASH");
    assertCafFieldEquals("FULL_NAME", "Ahmed St. George");
    assertCcapFieldEquals("TRIBAL_NATION", "Bois Forte");
    assertCafFieldEquals("FULL_NAME_0", householdMemberFullName);
    assertCafFieldEquals("PROGRAMS_0", "CCAP, CERTAIN_POPS");
    assertCafFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "SNAP");
    assertCafFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "CCAP");
    assertCafFieldEquals("GROSS_MONTHLY_INCOME_0", "120.00");
    assertCafFieldEquals("CREATED_DATE", "2020-01-01");
    assertCafFieldEquals("HEATING_COOLING_SELECTION", "ONE_SELECTED");
    assertCafFieldEquals("WATER_SEWER_SELECTION", "NEITHER_SELECTED");
    assertCafFieldEquals("ELECTRICITY", "No");
    assertCafFieldEquals("GARBAGE_REMOVAL", "No");
    assertCafFieldEquals("COOKING_FUEL", "No");
    assertCafFieldEquals("PHONE", "No");
    assertCafFieldEquals("APPLICANT_MAILING_ZIPCODE", "03104");
    assertCafFieldEquals("APPLICANT_MAILING_CITY", "Cooltown");
    assertCafFieldEquals("APPLICANT_MAILING_STATE", "CA");
    assertCafFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", "smarty street");
    assertCafFieldEquals("APPLICANT_MAILING_APT_NUMBER", "1b");
    assertCafFieldEquals("SSI", "No");
    assertCafFieldEquals("VETERANS_BENEFITS", "No");
    assertCafFieldEquals("UNEMPLOYMENT", "No");
    assertCafFieldEquals("WORKERS_COMPENSATION", "No");
    assertCafFieldEquals("RETIREMENT", "No");
    assertCafFieldEquals("CHILD_OR_SPOUSAL_SUPPORT", "No");
    assertCafFieldEquals("TRIBAL_PAYMENTS", "No");
    assertCafFieldEquals("HOMEOWNERS_INSURANCE", "No");
    assertCafFieldEquals("REAL_ESTATE_TAXES", "No");
    assertCafFieldEquals("ASSOCIATION_FEES", "No");
    assertCafFieldEquals("ROOM_AND_BOARD", "Yes");
    assertCafFieldEquals("RECEIVED_LIHEAP", "Yes");
    assertCafFieldEquals("REGISTER_TO_VOTE", "Yes");
    assertCafFieldEquals("SELF_EMPLOYED", "Yes");
    assertCafFieldEquals("SELF_EMPLOYED_GROSS_MONTHLY_EARNINGS", "see question 9");
    assertCafFieldEquals("PAY_FREQUENCY_0", "Hourly");
    assertCafFieldEquals("APPLICANT_HOME_APT_NUMBER", "someApartmentNumber");
    assertCafFieldEquals("APPLICANT_HOME_CITY", "someCity");
    assertCafFieldEquals("APPLICANT_HOME_STATE", "MN");
    assertCafFieldEquals("APPLICANT_HOME_ZIPCODE", "12345");
    assertCafFieldEquals("LIVING_SITUATION", "HOTEL_OR_MOTEL");
    assertCafFieldEquals("MEDICAL_EXPENSES_SELECTION", "ONE_SELECTED");
    assertCafFieldEquals("EMPLOYEE_FULL_NAME_0", householdMemberFullName);
    assertCafFieldEquals("WHO_IS_PREGNANT", "Ahmed St. George");
    assertCafFieldEquals("IS_US_CITIZEN", "No");
    assertCafFieldEquals("IS_US_CITIZEN_0", "Yes");
    assertCafFieldEquals("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE", "ENGLISH");
    assertCafFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH");
    assertCafFieldEquals("NEED_INTERPRETER", "Yes");
    assertCafFieldEquals("FOOD", "Yes");
    assertCafFieldEquals("CASH", "Yes");
    assertCafFieldEquals("CCAP", "Yes");
    assertCafFieldEquals("EMERGENCY", "Yes");
    assertCafFieldEquals("GRH", "Yes");
    assertCafFieldEquals("TANF", "Yes");
    assertCafFieldEquals("APPLICANT_FIRST_NAME", "Ahmed");
    assertCafFieldEquals("APPLICANT_LAST_NAME", "St. George");
    assertCafFieldEquals("APPLICANT_OTHER_NAME", "defaultOtherName");
    assertCafFieldEquals("DATE_OF_BIRTH", "01/12/1928");
    assertCafFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
    assertCafFieldEquals("MARITAL_STATUS", "NEVER_MARRIED");
    assertCafFieldEquals("APPLICANT_SEX", "FEMALE");
    assertCafFieldEquals("DATE_OF_MOVING_TO_MN", "10/20/1993");
    assertCafFieldEquals("APPLICANT_PREVIOUS_STATE", "Chicago");
    assertCafFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
    assertCafFieldEquals("PREPARING_MEALS_TOGETHER", "Yes");
    assertCafFieldEquals("GOING_TO_SCHOOL", "Yes");
    assertCafFieldEquals("IS_PREGNANT", "Yes");
    assertCafFieldEquals("IS_US_CITIZEN", "No");
    assertCafFieldEquals("EXPEDITED_QUESTION_2", "2468.00");
    assertCafFieldEquals("HOUSING_EXPENSES", "123321.50");
    assertCafFieldEquals("HEAT", "Yes");
    assertCafFieldEquals("SUPPORT_AND_CARE", "Yes");
    assertCafFieldEquals("MIGRANT_SEASONAL_FARM_WORKER", "No");
    assertCafFieldEquals("DRUG_FELONY", "No");
    assertCafFieldEquals("APPLICANT_SIGNATURE", "this is my signature");
    assertCafFieldEquals("HAS_DISABILITY", "Yes");
    assertCafFieldEquals("HAS_WORK_SITUATION", "No");
    assertCafFieldEquals("IS_WORKING", "No");
    assertCafFieldEquals("SOCIAL_SECURITY", "Yes");
    assertCafFieldEquals("SOCIAL_SECURITY_AMOUNT", "200.30");
    assertCafFieldEquals("EARN_LESS_MONEY_THIS_MONTH", "Yes");
    assertCafFieldEquals("ADDITIONAL_INCOME_INFO",
        "I also make a small amount of money from my lemonade stand.");
    assertCafFieldEquals("RENT", "Yes");
    assertCafFieldEquals("MORTGAGE", "Yes");
    assertCafFieldEquals("HOUSING_EXPENSES", "123321.50");
    assertCafFieldEquals("HAVE_SAVINGS", "Yes");
    assertCafFieldEquals("HAVE_INVESTMENTS", "Yes");
    assertCafFieldEquals("HAVE_VEHICLE", "Yes");
    assertCafFieldEquals("HAVE_SOLD_ASSETS", "No");
    assertCafFieldEquals("AUTHORIZED_REP_FILL_OUT_FORM", "Yes");
    assertCafFieldEquals("AUTHORIZED_REP_GET_NOTICES", "Yes");
    assertCafFieldEquals("AUTHORIZED_REP_SPEND_ON_YOUR_BEHALF", "Yes");
    assertCafFieldEquals("AUTHORIZED_REP_NAME", "defaultFirstName defaultLastName");
    assertCafFieldEquals("AUTHORIZED_REP_ADDRESS", "someStreetAddress");
    assertCafFieldEquals("AUTHORIZED_REP_CITY", "someCity");
    assertCafFieldEquals("AUTHORIZED_REP_ZIP_CODE", "12345");
    assertCafFieldEquals("AUTHORIZED_REP_PHONE_NUMBER", "(723) 456-7890");
    assertCafFieldEquals("ADDITIONAL_APPLICATION_INFO",
        "I need you to contact my work for proof of termination");
    assertCafFieldEquals("EMPLOYERS_NAME_0", "some employer");
    assertCafFieldEquals("HOURLY_WAGE_0", "1.00");
    assertCafFieldEquals("LAST_NAME_0", "householdMemberLastName");
    assertCafFieldEquals("SEX_0", "MALE");
    assertCafFieldEquals("DATE_OF_BIRTH_0", "09/14/2018");
    assertCafFieldEquals("DATE_OF_MOVING_TO_MN_0", "02");
    assertCafFieldEquals("SSN_0", "XXX-XX-XXXX");
    assertCafFieldEquals("FIRST_NAME_0", "householdMemberFirstName");
    assertCafFieldEquals("PREVIOUS_STATE_0", "Illinois");
    assertCafFieldEquals("OTHER_NAME_0", "houseHoldyMcMemberson");
    assertCafFieldEquals("CCAP_0", "Yes");
    assertCafFieldEquals("RELATIONSHIP_0", "child");
    assertCafFieldEquals("MARITAL_STATUS_0", "NEVER_MARRIED");
    assertCafFieldEquals("GROSS_MONTHLY_INCOME_0", "120.00");
    assertCafFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "someStreetAddress");
    assertCafFieldEquals("MONEY_MADE_LAST_MONTH", "920.00");
    assertCafFieldEquals("BLACK_OR_AFRICAN_AMERICAN", "Yes");
    assertCafFieldEquals("HISPANIC_LATINO_OR_SPANISH_NO", "Yes");
    
    // CERTAIN POPS
    assertCertainPopsFieldEquals("APPLICATION_ID", applicationId);
    assertCertainPopsFieldEquals("SUBMISSION_DATETIME", "01/01/2020 at 04:15 AM");
    assertCertainPopsFieldEquals("PAY_FREQUENCY_0", "Hourly");
    assertCertainPopsFieldEquals("EMPLOYEE_FULL_NAME_0", householdMemberFullName);
    assertCertainPopsFieldEquals("DATE_OF_BIRTH", "01/12/1928");
    assertCertainPopsFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
    assertCertainPopsFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
    assertCertainPopsFieldEquals("APPLICANT_EMAIL", "some@example.com");
    assertCertainPopsFieldEquals("PHONE_OPTIN", "Yes");
    assertCertainPopsFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", "");
    assertCertainPopsFieldEquals("EMPLOYERS_NAME_0", "some employer");
    assertCertainPopsFieldEquals("INCOME_PER_PAY_PERIOD_0", "1.00");
    assertCertainPopsFieldEquals("DATE_OF_BIRTH_0", "09/14/2018");
    assertCertainPopsFieldEquals("SSN_0", "XXX-XX-XXXX");
    assertCertainPopsFieldEquals("COUNTY_INSTRUCTIONS",
            """
                This application was submitted to Mille Lacs Band of Ojibwe and Chisago County with the information that you provided. Some parts of this application will be blank. A caseworker will follow up with you if additional information is needed.

                For more support, you can call Mille Lacs Band of Ojibwe (320-532-7407) and Chisago County (888-234-1246).""");
    assertCertainPopsFieldEquals("PROGRAMS", "SNAP, CCAP, EA, GRH, CERTAIN_POPS, TRIBAL TANF, CASH");
    assertCertainPopsFieldEquals("FULL_NAME", "Ahmed St. George");
    assertCertainPopsFieldEquals("TRIBAL_NATION", "Bois Forte");
    assertCertainPopsFieldEquals("FULL_NAME_0", householdMemberFullName);
    assertCertainPopsFieldEquals("PROGRAMS_0", "CCAP, CERTAIN_POPS");
    assertCertainPopsFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "SNAP");
    assertCertainPopsFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "CCAP");
    assertCertainPopsFieldEquals("APPLICANT_FIRST_NAME", "Ahmed");
    assertCertainPopsFieldEquals("APPLICANT_LAST_NAME", "St. George");
    assertCertainPopsFieldEquals("DATE_OF_BIRTH", "01/12/1928");
    assertCertainPopsFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
    assertCertainPopsFieldEquals("MARITAL_STATUS", "NEVER_MARRIED");
    assertCertainPopsFieldEquals("APPLICANT_SEX", "FEMALE");
    assertCertainPopsFieldEquals("BLIND", "Yes");
    assertCertainPopsFieldEquals("APPLICANT_IS_PREGNANT", "Yes");
    assertCertainPopsFieldEquals("HAS_PHYSICAL_MENTAL_HEALTH_CONDITION", "Yes");
    assertCertainPopsFieldEquals("DISABILITY_DETERMINATION", "Yes");
    assertCertainPopsFieldEquals("NEED_LONG_TERM_CARE", "Yes");
    assertCertainPopsFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH");
    assertCertainPopsFieldEquals("NEED_INTERPRETER", "Yes");
    assertCertainPopsFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "someStreetAddress");
    assertCertainPopsFieldEquals("APPLICANT_HOME_CITY", "someCity");
    assertCertainPopsFieldEquals("APPLICANT_HOME_STATE", "MN");
    assertCertainPopsFieldEquals("APPLICANT_HOME_ZIPCODE", "12345");
    assertCertainPopsFieldEquals("APPLICANT_MAILING_ZIPCODE", "03104");
    assertCertainPopsFieldEquals("APPLICANT_MAILING_CITY", "Cooltown");
    assertCertainPopsFieldEquals("APPLICANT_MAILING_STATE", "CA");
    assertCertainPopsFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", "smarty street");
    assertCertainPopsFieldEquals("APPLICANT_MAILING_COUNTY", "someCounty");
    assertCertainPopsFieldEquals("MEDICAL_IN_OTHER_STATE", "Yes");
    assertCertainPopsFieldEquals("LIVING_SITUATION", "HOTEL_OR_MOTEL");
    assertCertainPopsFieldEquals("HH_HEALTHCARE_COVERAGE_0", "Yes");
    assertCertainPopsFieldEquals("FIRST_NAME_0", "householdMemberFirstName");
    assertCertainPopsFieldEquals("MI_0", "");
    assertCertainPopsFieldEquals("LAST_NAME_0", "householdMemberLastName");
    assertCertainPopsFieldEquals("DATE_OF_BIRTH_0", "09/14/2018");
    assertCertainPopsFieldEquals("RELATIONSHIP_0", "child");
    assertCertainPopsFieldEquals("SEX_0", "MALE");
    assertCertainPopsFieldEquals("MARITAL_STATUS_0", "NEVER_MARRIED");
    assertCertainPopsFieldEquals("SSN_YESNO_0", "Yes");
    assertCertainPopsFieldEquals("SSN_0", "XXX-XX-XXXX");
    assertCertainPopsFieldEquals("IS_US_CITIZEN", "No");
    assertCertainPopsFieldEquals("NAME_OF_NON_US_CITIZEN_0", "Ahmed St. George");
    assertCertainPopsFieldEquals("ALIEN_ID_0", "A12345678");
    assertCertainPopsFieldEquals("WANT_AUTHORIZED_REP", "Yes");
    assertCertainPopsFieldEquals("RETROACTIVE_COVERAGE_HELP", "Yes");
    assertCertainPopsFieldEquals("RETROACTIVE_APPLICANT_FULLNAME_0", "Ahmed St. George");
    assertCertainPopsFieldEquals("RETROACTIVE_COVERAGE_MONTH_0", "1");
    //assertCertainPopsFieldEquals("SELF_EMPLOYED", "Yes");
    assertCertainPopsFieldEquals("IS_WORKING", "No");
    //assertCertainPopsFieldEquals("NO_CP_UNEARNED_INCOME", "Yes");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_PERSON_1", "Ahmed St. George");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_TYPE_1_1", "Social Security");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_AMOUNT_1_1", "200.30");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_FREQUENCY_1_1", "Monthly");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_TYPE_1_2", "Insurance payments");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_AMOUNT_1_2", "100.00");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_FREQUENCY_1_2", "Monthly");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_TYPE_1_3", "Trust money");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_AMOUNT_1_3", "100.00");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_FREQUENCY_1_3", "Monthly");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_TYPE_1_4", "Rental income");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_AMOUNT_1_4", "100.00");
    assertCertainPopsFieldEquals("CP_UNEARNED_INCOME_FREQUENCY_1_4", "Monthly");
    assertCertainPopsFieldEquals("BLIND_OR_HAS_DISABILITY", "Yes");
    assertCertainPopsFieldEquals("WHO_HAS_DISABILITY_0", "Ahmed St. George");
    assertCertainPopsFieldEquals("CASH_AMOUNT", "1234");
    assertCertainPopsFieldEquals("HAVE_INVESTMENTS", "Yes");
    assertCertainPopsFieldEquals("INVESTMENT_OWNER_FULL_NAME_0", "Ahmed St. George");
    assertCertainPopsFieldEquals("INVESTMENT_TYPE_0", "stocks");
    assertCertainPopsFieldEquals("HAVE_REAL_ESTATE", "Yes");
    assertCertainPopsFieldEquals("REAL_ESTATE_OWNER_FULL_NAME_0", "Ahmed St. George");
    assertCertainPopsFieldEquals("HAVE_CONTRACTS_NOTES_AGREEMENTS", "No");
    assertCertainPopsFieldEquals("HAVE_VEHICLE", "Yes");
    assertCertainPopsFieldEquals("VEHICLE_OWNER_FULL_NAME_0", "Ahmed St. George");
    assertCertainPopsFieldEquals("HAVE_TRUST_OR_ANNUITY", "No");
    assertCertainPopsFieldEquals("HAVE_LIFE_INSURANCE", "No");
    assertCertainPopsFieldEquals("HAVE_BURIAL_ACCOUNT", "No");
    assertCertainPopsFieldEquals("HAVE_OWNERSHIP_BUSINESS", "No");
    assertCertainPopsFieldEquals("HAVE_OTHER_ASSETS", "No");
    assertCertainPopsFieldEquals("HAD_A_PAST_ACCIDENT_OR_INJURY", "Yes");
    assertCertainPopsFieldEquals("HAVE_HEALTHCARE_COVERAGE", "Yes");
    assertCertainPopsFieldEquals("APPLICANT_SIGNATURE", "this is my signature");
    assertCertainPopsFieldEquals("CREATED_DATE", "2020-01-01");
    assertCertainPopsFieldEquals("AUTHORIZED_REP_NAME", "defaultFirstName defaultLastName");
    assertCertainPopsFieldEquals("AUTHORIZED_REP_ADDRESS", "someStreetAddress");
    assertCertainPopsFieldEquals("AUTHORIZED_REP_CITY", "someCity");
    assertCertainPopsFieldEquals("AUTHORIZED_REP_ZIP_CODE", "12345");
    assertCertainPopsFieldEquals("AUTHORIZED_REP_PHONE_NUMBER", "(723) 456-7890");
    assertCertainPopsFieldEquals("CP_SUPPLEMENT", "\n\nQUESTION 11 continued:\nPerson 1, Ahmed St. George:\n  5) Interest or dividends, 100.00, Monthly\n  6) Healthcare reimbursement, 100.00, Monthly\n  7) Contract for Deed, 100.00, Monthly\n  8) Benefits programs, 100.00, Monthly\n  9) Other payments, 100.00, Monthly");
    
    
    assertApplicationSubmittedEventWasPublished(applicationId, FULL, 8);
  }
  

	private void selectProgramsWithoutCertainPopsAndEnterPersonalInfo() {
		List<String> programSelections = List.of(PROGRAM_SNAP, PROGRAM_CCAP, PROGRAM_EA, PROGRAM_GRH);
		// Program Selection
		programSelections.forEach(program -> testPage.enter("programs", program));
		testPage.clickContinue();
		// Getting to know you (Personal Info intro page)
		testPage.clickContinue();
		testPage.clickContinue();
		// Personal Info
		testPage.enter("firstName", "Ahmed");
		testPage.enter("lastName", "St. George");
		testPage.enter("otherName", "defaultOtherName");
		// DOB is optional
		testPage.enter("maritalStatus", "Never married");
		testPage.enter("sex", "Female");
		testPage.enter("livedInMnWholeLife", "Yes");
		testPage.enter("moveToMnDate", "10/20/1993");
		testPage.enter("moveToMnPreviousCity", "Chicago");
		testPage.clickContinue();
		assertThat(testPage.getTitle()).isEqualTo("Home Address");
		testPage.goBack();
		testPage.enter("dateOfBirth", "01/12/1928");
		testPage.clickContinue();
	}

	protected void verifyHouseholdMemberCannotSelectCertainPops() {
		// Add 1 Household Member
		assertThat(testPage.getElementText("page-form")).contains("Roommates that you buy and prepare food with");
		testPage.enter("addHouseholdMembers", YES.getDisplayValue());
		testPage.clickContinue();
		assertThat(!(testPage.getElementText("page-form")).contains("Healthcare for Seniors and People with Disabilities"));
	}
  
	private void selectAllProgramsAndVerifyApplicantIsQualifiedForCertainPops() {
		List<String> programSelectionsWithCP = List.of(PROGRAM_SNAP, PROGRAM_CCAP, PROGRAM_EA, PROGRAM_GRH,
				PROGRAM_CERTAIN_POPS);
		testPage.enter("programs", PROGRAM_NONE);// reset programs
		// Program Selection
		programSelectionsWithCP.forEach(program -> testPage.enter("programs", program));
		testPage.clickContinue();

		// Test Certain pops offboarding flow first by selecting None of the above
		testPage.enter("basicCriteria", "None of the above");
		testPage.clickContinue();
		assertThat(testPage.getTitle()).isEqualTo("Certain Pops Offboarding");
		testPage.clickContinue();
		assertThat(testPage.getTitle()).isEqualTo("Add other programs");
		testPage.goBack();
		testPage.goBack();

		// Basic Criteria:
		testPage.enter("basicCriteria", "I am 65 years old or older");
		testPage.enter("basicCriteria", "I am blind");
		testPage.enter("basicCriteria", "I currently receive SSI or RSDI for a disability");
		testPage.enter("basicCriteria",	"I have a disability that has been certified by the Social Security Administration (SSA)");
		testPage.enter("basicCriteria",	"I have a disability that has been certified by the State Medical Review Team (SMRT)");
		testPage.enter("basicCriteria",	"I want to apply for Medical Assistance for Employed Persons with Disabilities (MA-EPD)");
		testPage.enter("basicCriteria", "I have Medicare and need help with my costs");
		testPage.clickContinue();
		assertThat(testPage.getTitle()).isEqualTo("Certain Pops Confirmation");
		testPage.clickContinue();
		assertThat(testPage.getTitle()).isEqualTo("Expedited Notice");
		testPage.clickContinue();

		// Getting to know you (Personal Info intro page)
		testPage.clickContinue();
		// Personal Info
		testPage.enter("firstName", "Ahmed");
		testPage.enter("lastName", "St. George");
		testPage.enter("otherName", "defaultOtherName");
		// DOB is optional
		testPage.enter("ssn", "123456789");
		// CP SSN check
		testPage.enter("noSSNCheck", "I don't have a social security number.");
		assertThat(testPage.getCheckboxValues("noSSNCheck")).contains("I don't have a social security number.",
				"I don't have a social security number.");
		testPage.enter("appliedForSSN", "Yes");
		testPage.clickContinue();
		// SSN textbox is filled and Checkbox is checked, so page won't advance and error shows
		assertThat(testPage.getTitle()).contains("Personal Info");
		testPage.enter("noSSNCheck", "I don't have a social security number.");// deselect the SSN checkbox
		testPage.enter("maritalStatus", "Never married");
		testPage.enter("sex", "Female");
		testPage.enter("livedInMnWholeLife", "Yes");
		testPage.enter("moveToMnDate", "10/20/1993");
		testPage.enter("moveToMnPreviousCity", "Chicago");
		testPage.clickContinue();
		assertThat(testPage.getTitle()).isEqualTo("Home Address");
		testPage.goBack();
		testPage.enter("dateOfBirth", "01/12/1928");
		testPage.clickContinue();

	}

  private void addSpouseAndVerifySpouseCanSelectCertainPops() {
		testPage.enter("addHouseholdMembers", YES.getDisplayValue());
		testPage.clickContinue();
	    assertThat(testPage.getElementText("page-form")).contains("Healthcare for Seniors and People with Disabilities");
	    testPage.enter("firstName", "Celia");
	    testPage.enter("lastName", "St. George");
	    testPage.enter("dateOfBirth", "10/15/1950");
	    testPage.enter("maritalStatus", "Married, living with spouse");
	    testPage.enter("sex", "Female");
	    testPage.enter("livedInMnWholeLife", "No");
	    testPage.enter("relationship", "My spouse (e.g. wife, husband)");
	    testPage.enter("programs", "None");
	    // This javascript scrolls the page to the bottom.  Shouldn't be necessary but without
	    // the scroll clickContinue doesn't seem to advance to the next page.
	    JavascriptExecutor js = ((JavascriptExecutor) driver);
	    js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	    testPage.clickContinue();
  }
  
	private void addHouseholdMemberToVerifySpouseCannotBeSelected() {
		testPage.clickLink("Add a person");
		String householdMemberFirstName = "householdMemberFirstName";
		String householdMemberLastName = "householdMemberLastName";
		testPage.enter("firstName", householdMemberFirstName);
		testPage.enter("lastName", householdMemberLastName);
		testPage.enter("otherName", "houseHoldyMcMemberson");
		testPage.enter("dateOfBirth", "09/14/2018");
		testPage.enter("maritalStatus", "Never married");
		testPage.enter("sex", "Male");
		testPage.enter("livedInMnWholeLife", "Yes"); // actually means they MOVED HERE
		testPage.enter("moveToMnDate", "02/18/1950");
		testPage.enter("moveToMnPreviousState", "Illinois");
		Select relationshipSelectWithRemovedSpouseOption = new Select(driver.findElement(By.id("relationship")));
		assertThat(relationshipSelectWithRemovedSpouseOption.getOptions().stream()
				.noneMatch(option -> option.getText().equals("My spouse (e.g. wife, husband)"))).isTrue();
		testPage.enter("relationship", "My child");
		testPage.enter("programs", PROGRAM_CCAP);
		// Assert that the programs follow up questions are shown when a program is selected
		WebElement programsFollowUp = testPage.findElementById("programs-follow-up");
		assertThat(programsFollowUp.getCssValue("display")).isEqualTo("block");
		// Assert that the programs follow up is hidden when none is selected
		testPage.enter("programs", PROGRAM_NONE);
		assertThat(programsFollowUp.getCssValue("display")).isEqualTo("none");
		testPage.enter("programs", PROGRAM_CCAP);
		testPage.enter("programs", PROGRAM_CERTAIN_POPS);
		// Assert that the programs follow up shows again when a program is selected after having selected none
		assertThat(programsFollowUp.getCssValue("display")).isEqualTo("block");
		testPage.enter("ssn", "987654321");
		testPage.clickContinue();
	}
  
	protected void removeSpouseAndVerifySpouseCanBeSelectedForNewHouseholdMember() {
		// You are about to delete householdMember0 as a household member.
		driver.findElement(By.id("iteration0-delete")).click();
		testPage.clickButton("Yes, remove them");
		// Check that My Spouse is now an option again after deleting the spouse
		testPage.clickLink("Add a person");
		Select relationshipSelectWithSpouseOption = new Select(driver.findElement(By.id("relationship")));
		assertThat(relationshipSelectWithSpouseOption.getOptions().stream()
				.anyMatch(option -> option.getText().equals("My spouse (e.g. wife, husband)"))).isTrue();
		testPage.goBack();
	}

  
  /**
   * Call this only if phone and email have already been entered and tested before.
   */
  private void goToContactAndReview() {   
    // How can we get in touch with you?
    testPage.enter("phoneNumber", "7234567890");
    testPage.enter("email", "some@example.com");
    testPage.clickContinue();
    testPage.clickLink("This looks correct");
  }


  private void testDocumentUploads() {
    // Uploading a file should change the page styling
    uploadButtonDisabledCheck();
    deleteAFile();
    assertStylingOfEmptyDocumentUploadPage();
    uploadJpgFile();
    waitForDocumentUploadToComplete();
    assertStylingOfNonEmptyDocumentUploadPage();

    // Deleting the only uploaded document should keep you on the upload document screen
    assertThat(driver.findElements(By.linkText("delete")).size()).isEqualTo(1);
    deleteAFile();

    assertThat(testPage.getTitle()).isEqualTo(
        "Upload documents");
    assertThat(driver.findElements(By.linkText("delete")).size()).isEqualTo(0);

    assertStylingOfEmptyDocumentUploadPage();

    // Uploading multiple docs should work
    uploadJpgFile();
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText())
        .isEqualTo("1 file added");
    uploadPdfFile();
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText())
        .isEqualTo("2 files added");
    uploadFile(getAbsoluteFilepathString(
        "pdf-without-acroform.pdf")); // Assert that we can still upload PDFs without acroforms
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText())
        .isEqualTo("3 files added");
    waitForDocumentUploadToComplete();
    assertThat(driver.findElements(By.linkText("delete")).size()).isEqualTo(3);

    // After deleting a file, the order of the remaining files should be maintained
    deleteAFile();
    assertThat(driver.findElement(By.id("number-of-uploaded-files")).getText())
        .isEqualTo("2 files added");
    var filenameTextElements = driver.findElements(By.className("filename-text"));
    var fileDetailsElements = driver.findElements(By.className("file-details"));
    assertFileDetailsAreCorrect(filenameTextElements, fileDetailsElements, 0, "test-caf", "pdf",
        "0.4", "MB");
    assertFileDetailsAreCorrect(filenameTextElements, fileDetailsElements, 1, "shiba", "jpg",
        "19.1", "KB");
  }

  private void assertFileDetailsAreCorrect(List<WebElement> filenameTextElements,
      List<WebElement> fileDetailsElements, int index,
      String filenameWithoutExtension, String extension, String size,
      String sizeUnit) {
    // test-caf.pdf
    var filename = getAttributeForElementAtIndex(filenameTextElements, index, "innerHTML");
    var fileDetails = getAttributeForElementAtIndex(fileDetailsElements, index, "innerHTML");

    assertThat(filename).contains(filenameWithoutExtension);
    assertThat(filename).contains(extension);
    assertThat(fileDetails).contains(size);
    assertThat(fileDetails).contains(sizeUnit);
  }

  private void assertStylingOfNonEmptyDocumentUploadPage() {
    assertThat(driver.findElement(By.id("drag-and-drop-box")).getAttribute("class")).contains(
        "drag-and-drop-box-compact");
    assertThat(driver.findElement(By.id("upload-button"))
        .getAttribute("class")).contains("grid--item width-one-third");
    assertThat(driver.findElement(By.id("vertical-header-desktop")).getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElement(By.id("vertical-header-mobile")).getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElement(By.id("horizontal-header-desktop")).getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElement(By.id("horizontal-header-mobile")).getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElement(By.id("upload-doc-div")).getAttribute("class"))
        .doesNotContain("hidden");
  }

  private void assertStylingOfEmptyDocumentUploadPage() {
    assertThat(driver.findElement(By.id("drag-and-drop-box")).getAttribute("class")).doesNotContain(
        "drag-and-drop-box-compact");
    assertThat(driver.findElement(By.id("upload-button")).getAttribute("class")).doesNotContain(
        "grid--item width-one-third");
    assertThat(driver.findElement(By.id("vertical-header-desktop")).getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElement(By.id("vertical-header-mobile")).getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElement(By.id("horizontal-header-desktop")).getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElement(By.id("horizontal-header-mobile")).getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElement(By.id("upload-doc-div")).getAttribute("class")).contains(
        "hidden");
  }
}
