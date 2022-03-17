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
import java.util.List;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.testutilities.PercyTestPage;
import org.codeforamerica.shiba.testutilities.SuccessPage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
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
    when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.ON);

    // Assert intercom button is present on landing page
    await().atMost(5, SECONDS).until(() -> !driver.findElementsById("intercom-frame").isEmpty());
    assertThat(driver.findElementById("intercom-frame")).isNotNull();

    List<String> programSelections = List
        .of(PROGRAM_SNAP, PROGRAM_CCAP, PROGRAM_EA, PROGRAM_GRH, PROGRAM_CERTAIN_POPS);
    getToHomeAddress("Hennepin", programSelections);

    // Where are you currently Living?
    fillOutHomeAndMailingAddress("12345", "someCity", "someStreetAddress", "someApartmentNumber");
    fillOutContactAndReview(true);
    testPage.clickLink("This looks correct");

    // Add 1 Household Member
    assertThat(testPage.getElementText("page-form")).contains(
        "Roommates that you buy and prepare food with");
    testPage.enter("addHouseholdMembers", YES.getDisplayValue());
    testPage.clickContinue();

    String householdMemberFirstName = "householdMemberFirstName";
    String householdMemberLastName = "householdMemberLastName";
    String householdMemberFullName = householdMemberFirstName + " " + householdMemberLastName;
    testPage.enter("firstName", householdMemberFirstName);
    testPage.enter("lastName", householdMemberLastName);
    testPage.enter("otherName", "houseHoldyMcMemberson");
    testPage.enter("dateOfBirth", "09/14/2018");
    testPage.enter("maritalStatus", "Never married");
    testPage.enter("sex", "Male");
    testPage.enter("livedInMnWholeLife", "Yes"); // actually means they MOVED HERE
    testPage.enter("moveToMnDate", "02/18/1950");
    testPage.enter("moveToMnPreviousState", "Illinois");
    testPage.enter("relationship", "My child");
    testPage.enter("programs", PROGRAM_CCAP);
    // Assert that the programs follow up questions are shown when a program is selected
    WebElement programsFollowUp = testPage.findElementById("programs-follow-up");
    assertThat(programsFollowUp.getCssValue("display")).isEqualTo("block");
    // Assert that the programs follow up is hidden when none is selected
    testPage.enter("programs", PROGRAM_NONE);
    assertThat(programsFollowUp.getCssValue("display")).isEqualTo("none");
    testPage.enter("programs", PROGRAM_CCAP);
    // Assert that the programs follow up shows again when a program is selected after having selected none
    assertThat(programsFollowUp.getCssValue("display")).isEqualTo("block");
    testPage.enter("ssn", "987654321");
    testPage.clickContinue();

    // Add a spouse and assert spouse is no longer an option then delete -- Household member 2
    testPage.clickLink("Add a person");

    testPage.enter("firstName", "householdMember2");
    testPage.enter("lastName", householdMemberLastName);
    testPage.enter("dateOfBirth", "10/15/1950");
    testPage.enter("maritalStatus", "Divorced");
    testPage.enter("sex", "Female");
    testPage.enter("livedInMnWholeLife", "No");
    testPage.enter("relationship", "My spouse (e.g. wife, husband)");
    testPage.enter("programs", "None");
    testPage.clickContinue();

    // Verify spouse option has been removed
    testPage.clickLink("Add a person");
    Select relationshipSelectWithRemovedSpouseOption = new Select(
        driver.findElementById("relationship"));
    assertThat(relationshipSelectWithRemovedSpouseOption.getOptions().stream()
        .noneMatch(option -> option.getText().equals("My spouse (e.g. wife, husband)"))).isTrue();
    testPage.goBack();

    // You are about to delete householdMember2 as a household member.
    driver.findElementById("iteration1-delete").click();
    testPage.clickButton("Yes, remove them");
    // Check that My Spouse is now an option again after deleting the spouse
    testPage.clickLink("Add a person");
    Select relationshipSelectWithSpouseOption = new Select(driver.findElementById("relationship"));
    assertThat(relationshipSelectWithSpouseOption.getOptions().stream()
        .anyMatch(option -> option.getText().equals("My spouse (e.g. wife, husband)"))).isTrue();
    testPage.goBack();
    testPage.clickButton("Yes, that's everyone");

    // Who are the children in need of childcare
    testPage.enter("whoNeedsChildCare", householdMemberFullName);
    testPage.clickContinue();

    // Who are the children that have a parent not living at home?
    testPage.enter("whoHasAParentNotLivingAtHome", householdMemberFullName);
    testPage.clickContinue();

    // Tell us the name of any parent living outside the home.
    String parentNotAtHomeName = "My child's parent";
    driver.findElementByName("whatAreTheParentsNames[]").sendKeys(parentNotAtHomeName);
    testPage.clickContinue();

    // Does everyone in your household buy and prepare food with you?
    testPage.enter("isPreparingMealsTogether", YES.getDisplayValue());

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
    testPage.enter("whoIsPregnant", "Me");
    testPage.clickContinue();

    // Is anyone in your household a migrant or seasonal farm worker?
    testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());

    // Is everyone in your household a U.S. Citizen?
    testPage.enter("isUsCitizen", NO.getDisplayValue());

    // Who is not a U.S Citizen?
    testPage.enter("whoIsNonCitizen", "Me");
    testPage.clickContinue();

    // Does anyone in your household have a physical or mental disability that prevents them from working?
    testPage.enter("hasDisability", NO.getDisplayValue());

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
    driver.findElementById("iteration1-delete").click();
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
    testPage.enter("socialSecurityAmount", "200.30");
    testPage.clickContinue();

    // Does anyone in your household get income from these other sources?
    testPage.enter("unearnedIncomeCcap", "Benefits programs like MFIP, DWP, GA, or Tribal TANF");
    testPage.enter("unearnedIncomeCcap", "Insurance Payments");
    testPage.enter("unearnedIncomeCcap", "Contract for Deed");
    testPage.enter("unearnedIncomeCcap", "Money from a Trust");
    testPage.enter("unearnedIncomeCcap", "Health Care Reimbursement");
    testPage.enter("unearnedIncomeCcap", "Interest/Dividends");
    testPage.enter("unearnedIncomeCcap", "Income from Other Sources");
    testPage.clickContinue();

    // Tell us how much money is received.
    testPage.enter("benefitsAmount", "10");
    testPage.enter("contractForDeedAmount", "20");
    testPage.enter("interestDividendsAmount", "30");
    testPage.enter("otherSourcesAmount", "40");
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

    // Does anyone in the household own a vehicle?
    testPage.enter("haveVehicle", YES.getDisplayValue());

    // Do anyone in the household own any real estate (not including the home you currently live in)?
    testPage.enter("ownRealEstate", YES.getDisplayValue());

    // Does anyone in the household have stocks, bonds or a 401k?
    testPage.enter("haveInvestments", NO.getDisplayValue());

    // Does anyone in the household have money in a bank account or debit card?
    testPage.enter("haveSavings", YES.getDisplayValue());

    // How much money is available?
    testPage.enter("liquidAssets", "1234");
    testPage.clickContinue();

    // Does your family have more than $1 million in assets?
    testPage.enter("haveMillionDollars", NO.getDisplayValue());

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
    testPage.clickButton("Submit");
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
    navigateTo("documentSubmitConfirmation");
    assertThat(driver.getTitle()).isEqualTo("Your next steps");
    testPage.clickContinue();

    SuccessPage successPage = new SuccessPage(driver);
    assertThat(successPage.findElementById("submission-date").getText()).contains(
        "Your application was submitted to Hennepin County (612-596-1300) and Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency (320-532-7407) on January 1, 2020.");
    applicationId = downloadPdfs();

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
        This application was submitted to Hennepin County with the information that you provided. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed.
                    
        For more support, you can call Hennepin County (612-596-1300).""");
    assertCcapFieldEquals("PROGRAMS", "SNAP, CCAP, EA, GRH, CERTAIN_POPS, TRIBAL TANF, CASH");
    assertCcapFieldEquals("FULL_NAME", "Ahmed St. George");
    assertCcapFieldEquals("UTM_SOURCE", "");
    assertCcapFieldEquals("FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("TRIBAL_NATION", "Bois Forte");
    assertCcapFieldEquals("PROGRAMS_0", "CCAP");
    assertCcapFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
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
    assertCcapFieldEquals("BENEFITS", "Yes");
    assertCcapFieldEquals("INSURANCE_PAYMENTS", "Yes");
    assertCcapFieldEquals("CONTRACT_FOR_DEED", "Yes");
    assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT", "Yes");
    assertCcapFieldEquals("INTEREST_DIVIDENDS", "Yes");
    assertCcapFieldEquals("OTHER_SOURCES", "Yes");
    assertCcapFieldEquals("SELF_EMPLOYMENT_EMPLOYEE_FULL_NAME_0", householdMemberFullName);
    assertCcapFieldEquals("IS_US_CITIZEN_0", "Yes");
    assertCcapFieldEquals("SOCIAL_SECURITY_FREQUENCY", "Monthly");
    assertCcapFieldEquals("TRUST_MONEY_FREQUENCY", "Monthly");
    assertCcapFieldEquals("MEDICAL_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
    assertCcapFieldEquals("VISION_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
    assertCcapFieldEquals("DENTAL_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
    assertCcapFieldEquals("MEDICAL_INSURANCE_PREMIUM_AMOUNT", "10.90");
    assertCcapFieldEquals("DENTAL_INSURANCE_PREMIUM_AMOUNT", "12.34");
    assertCcapFieldEquals("VISION_INSURANCE_PREMIUM_AMOUNT", "56.35");
    assertCcapFieldEquals("IS_WORKING", "Yes");
    assertCcapFieldEquals("SOCIAL_SECURITY", "Yes");
    assertCcapFieldEquals("SOCIAL_SECURITY_AMOUNT", "200.30");
    assertCcapFieldEquals("TRUST_MONEY", "Yes");
    assertCcapFieldEquals("TRUST_MONEY_AMOUNT", "");
    assertCcapFieldEquals("BENEFITS", "Yes");
    assertCcapFieldEquals("INSURANCE_PAYMENTS", "Yes");
    assertCcapFieldEquals("CONTRACT_FOR_DEED", "Yes");
    assertCcapFieldEquals("TRUST_MONEY", "Yes");
    assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT", "Yes");
    assertCcapFieldEquals("INTEREST_DIVIDENDS", "Yes");
    assertCcapFieldEquals("OTHER_SOURCES", "Yes");
    assertCcapFieldEquals("BENEFITS_AMOUNT", "10");
    assertCcapFieldEquals("INSURANCE_PAYMENTS_AMOUNT", "");
    assertCcapFieldEquals("CONTRACT_FOR_DEED_AMOUNT", "20");
    assertCcapFieldEquals("TRUST_MONEY_AMOUNT", "");
    assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT_AMOUNT", "");
    assertCcapFieldEquals("INTEREST_DIVIDENDS_AMOUNT", "30");
    assertCcapFieldEquals("OTHER_SOURCES_AMOUNT", "40");
    assertCcapFieldEquals("BENEFITS_FREQUENCY", "Monthly");
    assertCcapFieldEquals("INSURANCE_PAYMENTS_FREQUENCY", "Monthly");
    assertCcapFieldEquals("CONTRACT_FOR_DEED_FREQUENCY", "Monthly");
    assertCcapFieldEquals("TRUST_MONEY_FREQUENCY", "Monthly");
    assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT_FREQUENCY", "Monthly");
    assertCcapFieldEquals("INTEREST_DIVIDENDS_FREQUENCY", "Monthly");
    assertCcapFieldEquals("OTHER_SOURCES_FREQUENCY", "Monthly");
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
        "This application was submitted to Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency and Hennepin County with the information that you provided. Some parts of this application will be blank. A county worker will follow up with you if additional information is needed.\n\n"
            + "For more support, you can call Mille Lacs Band of Ojibwe Tribal Nation Servicing Agency (320-532-7407) and Hennepin County (612-596-1300).");
    assertCafFieldEquals("PROGRAMS", "SNAP, CCAP, EA, GRH, CERTAIN_POPS, TRIBAL TANF, CASH");
    assertCafFieldEquals("FULL_NAME", "Ahmed St. George");
    assertCcapFieldEquals("TRIBAL_NATION", "Bois Forte");
    assertCafFieldEquals("FULL_NAME_0", householdMemberFullName);
    assertCafFieldEquals("PROGRAMS_0", "CCAP");
    assertCafFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
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
    assertCafFieldEquals("EXPEDITED_QUESTION_2", "1234.00");
    assertCafFieldEquals("HOUSING_EXPENSES", "123321.50");
    assertCafFieldEquals("HEAT", "Yes");
    assertCafFieldEquals("SUPPORT_AND_CARE", "Yes");
    assertCafFieldEquals("MIGRANT_SEASONAL_FARM_WORKER", "No");
    assertCafFieldEquals("DRUG_FELONY", "No");
    assertCafFieldEquals("APPLICANT_SIGNATURE", "this is my signature");
    assertCafFieldEquals("HAS_DISABILITY", "No");
    assertCafFieldEquals("HAS_WORK_SITUATION", "No");
    assertCafFieldEquals("IS_WORKING", "Yes");
    assertCafFieldEquals("SOCIAL_SECURITY", "Yes");
    assertCafFieldEquals("SOCIAL_SECURITY_AMOUNT", "200.30");
    assertCafFieldEquals("EARN_LESS_MONEY_THIS_MONTH", "Yes");
    assertCafFieldEquals("ADDITIONAL_INCOME_INFO",
        "I also make a small amount of money from my lemonade stand.");
    assertCafFieldEquals("RENT", "Yes");
    assertCafFieldEquals("MORTGAGE", "Yes");
    assertCafFieldEquals("HOUSING_EXPENSES", "123321.50");
    assertCafFieldEquals("HAVE_SAVINGS", "Yes");
    assertCafFieldEquals("HAVE_INVESTMENTS", "No");
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
    assertCafFieldEquals("MONEY_MADE_LAST_MONTH", "120.00");
    assertCafFieldEquals("BLACK_OR_AFRICAN_AMERICAN", "Yes");
    assertCafFieldEquals("HISPANIC_LATINO_OR_SPANISH_NO", "Yes");

    assertApplicationSubmittedEventWasPublished(applicationId, FULL, 8);
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

    assertThat(testPage.getTitle()).isEqualTo("Upload Documents");
    assertThat(driver.findElements(By.linkText("delete")).size()).isEqualTo(0);

    assertStylingOfEmptyDocumentUploadPage();

    // Uploading multiple docs should work
    uploadJpgFile();
    assertThat(driver.findElementById("number-of-uploaded-files").getText())
        .isEqualTo("1 file added");
    uploadPdfFile();
    assertThat(driver.findElementById("number-of-uploaded-files").getText())
        .isEqualTo("2 files added");
    uploadFile(getAbsoluteFilepathString(
        "pdf-without-acroform.pdf")); // Assert that we can still upload PDFs without acroforms
    assertThat(driver.findElementById("number-of-uploaded-files").getText())
        .isEqualTo("3 files added");
    waitForDocumentUploadToComplete();
    assertThat(driver.findElements(By.linkText("delete")).size()).isEqualTo(3);

    // After deleting a file, the order of the remaining files should be maintained
    deleteAFile();
    assertThat(driver.findElementById("number-of-uploaded-files").getText())
        .isEqualTo("2 files added");
    var filenameTextElements = driver.findElementsByClassName("filename-text");
    var fileDetailsElements = driver.findElementsByClassName("file-details");
    assertFileDetailsAreCorrect(filenameTextElements, fileDetailsElements, 0, "test-caf", "pdf",
        "0.4", "MB");
    assertFileDetailsAreCorrect(filenameTextElements, fileDetailsElements, 1, "shiba", "jpg",
        "51.7", "KB");
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
    assertThat(driver.findElementById("drag-and-drop-box").getAttribute("class")).contains(
        "drag-and-drop-box-compact");
    assertThat(driver.findElementById("upload-button")
        .getAttribute("class")).contains("grid--item width-one-third");
    assertThat(driver.findElementById("vertical-header-desktop").getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElementById("vertical-header-mobile").getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElementById("horizontal-header-desktop").getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElementById("horizontal-header-mobile").getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElementById("upload-doc-div").getAttribute("class"))
        .doesNotContain("hidden");
  }

  private void assertStylingOfEmptyDocumentUploadPage() {
    assertThat(driver.findElementById("drag-and-drop-box").getAttribute("class")).doesNotContain(
        "drag-and-drop-box-compact");
    assertThat(driver.findElementById("upload-button").getAttribute("class")).doesNotContain(
        "grid--item width-one-third");
    assertThat(driver.findElementById("vertical-header-desktop").getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElementById("vertical-header-mobile").getAttribute("class"))
        .doesNotContain("hidden");
    assertThat(driver.findElementById("horizontal-header-desktop").getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElementById("horizontal-header-mobile").getAttribute("class"))
        .contains("hidden");
    assertThat(driver.findElementById("upload-doc-div").getAttribute("class")).contains("hidden");
  }
}
