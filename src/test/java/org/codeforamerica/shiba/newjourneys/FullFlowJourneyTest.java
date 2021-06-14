package org.codeforamerica.shiba.newjourneys;

import org.codeforamerica.shiba.pages.journeys.JourneyTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.List;

import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

public class FullFlowJourneyTest extends JourneyTest {

    @Test
    void fullApplicationFlow() {
        List<String> programSelections = List.of(PROGRAM_SNAP, PROGRAM_CCAP, PROGRAM_EA, PROGRAM_CASH, PROGRAM_GRH);

        getToHomeAddress(programSelections);

        // Where are you currently Living?
        fillOutHomeAndMailingAddress("12345", "someCity", "someStreetAddress", "someApartmentNumber");
        testPage.clickLink("This looks correct");

        // Add 1 Household Member
        testPage.enter("addHouseholdMembers", YES.getDisplayValue());
        testPage.clickContinue();

        String householdMemberFirstName = "householdMemberFirstName";
        String householdMemberLastName = "householdMemberLastName";
        String householdMemberFullName = householdMemberFirstName + " " + householdMemberLastName;
        testPage.enter("firstName", householdMemberFirstName);
        testPage.enter("lastName", householdMemberLastName);
        testPage.enter("otherName", "houseHoldyMcMemberson");
        testPage.enter("dateOfBirth", "09/14/1950");
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", "Male");
        testPage.enter("livedInMnWholeLife", "Yes"); // actually means they MOVED HERE
        testPage.enter("moveToMnDate", "02/18/1950");
        testPage.enter("moveToMnPreviousState", "Illinois");
        testPage.enter("relationship", "my child");
        testPage.enter("programs", PROGRAM_CCAP);
        testPage.enter("ssn", "987654321");
        testPage.clickContinue();

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
        testPage.enter("livingSituation", "None of these");
        testPage.clickContinue();

        // Is anyone in your household going to school right now, either full or part-time?
        testPage.enter("goingToSchool", NO.getDisplayValue());

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

        // Income & Employment
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
        testPage.enter("unearnedIncomeCcap", "Money from a Trust");
        testPage.clickContinue();

        // Tell us how much money is received.
        testPage.enter("trustMoneyAmount", "200.15");
        testPage.clickContinue();

        // Do you think the household will earn less money this month than last month?
        testPage.enter("earnLessMoneyThisMonth", "Yes");
        driver.findElement(By.id("additionalIncomeInfo"))
                .sendKeys("I also make a small amount of money from my lemonade stand.");
        testPage.clickContinue();

        // Expenses & Deductions
        testPage.clickContinue();

        // Does anyone in your household pay for any of these?
        testPage.enter("homeExpenses", "Rent");
        testPage.enter("homeExpenses", "Mortgage");
        testPage.clickContinue();

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
        testPage.clickContinue();

        // Tell us how much money is paid.
        testPage.enter("dentalInsurancePremiumAmount", "12.34");
        testPage.enter("visionInsurancePremiumAmount", "56.35");
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
        testPage.enter("registerToVote", "Yes, send me more info");

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
        testPage.enter("spendOnYourBehalf", YES.getDisplayValue());

        // Let's get your helpers contact information
        testPage.enter("helpersFullName", "defaultFirstName defaultLastName");
        testPage.enter("helpersStreetAddress", "someStreetAddress");
        testPage.enter("helpersCity", "someCity");
        testPage.enter("helpersZipCode", "12345");
        testPage.enter("helpersPhoneNumber", "7234567890");
        testPage.clickContinue();

        // Is there anything else you want to share?
        driver.findElement(By.id("additionalInfo")).sendKeys("I need you to contact my work for proof of termination");
        testPage.clickContinue();

        // The legal stuff.
        testPage.enter("agreeToTerms", "I agree");
        testPage.enter("drugFelony", NO.getDisplayValue());
        testPage.clickContinue();

        String applicationId = signApplicationAndDownloadPdfs("this is my signature", true, true);

        // CCAP fields
        assertCcapFieldEquals("APPLICATION_ID", applicationId);
//        assertCcapFieldEquals("SUBMISSION_DATETIME", "06/14/2021 at 04:21 PM");
        assertCcapFieldEquals("PAY_FREQUENCY_0", "Hourly");
        assertCcapFieldEquals("EMPLOYEE_FULL_NAME_0", "householdMemberFirstName householdMemberLastName");
        assertCcapFieldEquals("DATE_OF_BIRTH", "01/12/1928");
        assertCcapFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
        assertCcapFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
        assertCcapFieldEquals("APPLICANT_EMAIL", "some@email.com");
        assertCcapFieldEquals("PHONE_OPTIN", "Yes");
        assertCcapFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", "");
        assertCcapFieldEquals("EMPLOYERS_NAME_0", "some employer");
        assertCcapFieldEquals("SELF_EMPLOYMENT_0", "Yes");
        assertCcapFieldEquals("INCOME_PER_PAY_PERIOD_0", "1.00");
        assertCcapFieldEquals("DATE_OF_BIRTH_0", "09/14/1950");
        assertCcapFieldEquals("SSN_0", "XXX-XX-XXXX");
        assertCcapFieldEquals("COUNTY_INSTRUCTIONS",
                              "This application was submitted. A caseworker at Hennepin County will help route your application to your county. For more support with your application, you can call Hennepin County at 612-596-1300.");
        assertCcapFieldEquals("PROGRAMS", "SNAP, CASH, CCAP, EA, GRH");
        assertCcapFieldEquals("FULL_NAME", "Ahmed St. George");
        assertCcapFieldEquals("UTM_SOURCE", "");
        assertCcapFieldEquals("FULL_NAME_0", "householdMemberFirstName householdMemberLastName");
        assertCcapFieldEquals("PROGRAMS_0", "CCAP");
        assertCcapFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
        assertCcapFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "");
        assertCcapFieldEquals("GROSS_MONTHLY_INCOME_0", "120.00");
        assertCcapFieldEquals("APPLICANT_MAILING_ZIPCODE", "03104");
        assertCcapFieldEquals("APPLICANT_MAILING_CITY", "Cooltown");
        assertCcapFieldEquals("APPLICANT_MAILING_STATE", "CA");
        assertCcapFieldEquals("APPLICANT_MAILING_STREET_ADDRESS", "smarty street");
        assertCcapFieldEquals("APPLICANT_HOME_CITY", "someCity");
        assertCcapFieldEquals("APPLICANT_HOME_STATE", "MN");
        assertCcapFieldEquals("APPLICANT_HOME_ZIPCODE", "12345");
        assertCcapFieldEquals("LIVING_SITUATION", "UNKNOWN");
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
        assertCcapFieldEquals("APPLICANT_EMAIL", "some@email.com");
        assertCcapFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "someStreetAddress (not permanent)");
        assertCcapFieldEquals("ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0", "");
        assertCcapFieldEquals("ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_0", "");
        assertCcapFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_0", "householdMemberFirstName householdMemberLastName");
        assertCcapFieldEquals("SSI", "No");
        assertCcapFieldEquals("VETERANS_BENEFITS", "No");
        assertCcapFieldEquals("UNEMPLOYMENT", "No");
        assertCcapFieldEquals("WORKERS_COMPENSATION", "No");
        assertCcapFieldEquals("RETIREMENT", "No");
        assertCcapFieldEquals("CHILD_OR_SPOUSAL_SUPPORT", "No");
        assertCcapFieldEquals("TRIBAL_PAYMENTS", "No");
        assertCcapFieldEquals("BENEFITS", "No");
        assertCcapFieldEquals("INSURANCE_PAYMENTS", "No");
        assertCcapFieldEquals("CONTRACT_FOR_DEED", "No");
        assertCcapFieldEquals("HEALTH_CARE_REIMBURSEMENT", "No");
        assertCcapFieldEquals("INTEREST_DIVIDENDS", "No");
        assertCcapFieldEquals("OTHER_SOURCES", "No");
        assertCcapFieldEquals("SELF_EMPLOYMENT_EMPLOYEE_FULL_NAME_0",
                              "householdMemberFirstName householdMemberLastName");
        assertCcapFieldEquals("IS_US_CITIZEN_0", "Yes");
        assertCcapFieldEquals("SOCIAL_SECURITY_FREQUENCY", "Monthly");
        assertCcapFieldEquals("TRUST_MONEY_FREQUENCY", "Monthly");
        assertCcapFieldEquals("VISION_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
        assertCcapFieldEquals("DENTAL_INSURANCE_PREMIUM_FREQUENCY", "Monthly");
        assertCcapFieldEquals("DENTAL_INSURANCE_PREMIUM_AMOUNT", "12.34");
        assertCcapFieldEquals("VISION_INSURANCE_PREMIUM_AMOUNT", "56.35");
        assertCcapFieldEquals("IS_WORKING", "Yes");
        assertCcapFieldEquals("SOCIAL_SECURITY", "Yes");
        assertCcapFieldEquals("SOCIAL_SECURITY_AMOUNT", "200.30");
        assertCcapFieldEquals("TRUST_MONEY", "Yes");
        assertCcapFieldEquals("TRUST_MONEY_AMOUNT", "200.15");
        assertCcapFieldEquals("EARN_LESS_MONEY_THIS_MONTH", "Yes");
        assertCcapFieldEquals("ADDITIONAL_INCOME_INFO", "I also make a small amount of money from my lemonade stand.");
        assertCcapFieldEquals("HAVE_MILLION_DOLLARS", "No");
        assertCcapFieldEquals("PARENT_NOT_LIVING_AT_HOME_0", "My child's parent");
        assertCcapFieldEquals("CHILD_FULL_NAME_0", "householdMemberFirstName householdMemberLastName");
        assertCcapFieldEquals("SELF_EMPLOYMENT_HOURS_A_WEEK_0", "30");
        assertCcapFieldEquals("LAST_NAME_0", "householdMemberLastName");
        assertCcapFieldEquals("SEX_0", "MALE");
        assertCcapFieldEquals("DATE_OF_BIRTH_0", "09/14/1950");
        assertCcapFieldEquals("SSN_0", "XXX-XX-XXXX");
        assertCcapFieldEquals("FIRST_NAME_0", "householdMemberFirstName");
        assertCcapFieldEquals("RELATIONSHIP_0", "my child");
        assertCcapFieldEquals("SELF_EMPLOYMENT_GROSS_MONTHLY_INCOME_0", "120.00");
        assertCcapFieldEquals("LIVING_WITH_FAMILY_OR_FRIENDS", "Off");
        assertCcapFieldEquals("CREATED_DATE", "2021-06-14");
        assertCcapFieldEquals("APPLICANT_SIGNATURE", "this is my signature");
        assertCcapFieldEquals("ADDITIONAL_APPLICATION_INFO", "I need you to contact my work for proof of termination");

        // CAF
        assertCafFieldEquals("APPLICATION_ID", applicationId);
//        assertCafFieldEquals("SUBMISSION_DATETIME","06/14/2021 at 04:24 PM");
        assertCafFieldEquals("PAY_FREQUENCY_0", "Hourly");
        assertCafFieldEquals("EMPLOYEE_FULL_NAME_0", "householdMemberFirstName householdMemberLastName");
        assertCafFieldEquals("DATE_OF_BIRTH", "01/12/1928");
        assertCafFieldEquals("APPLICANT_SSN", "XXX-XX-XXXX");
        assertCafFieldEquals("APPLICANT_PHONE_NUMBER", "(723) 456-7890");
        assertCafFieldEquals("APPLICANT_EMAIL", "some@email.com");
        assertCafFieldEquals("PHONE_OPTIN", "Yes");
        assertCafFieldEquals("ADDITIONAL_INFO_CASE_NUMBER", "");
        assertCafFieldEquals("EMPLOYERS_NAME_0", "some employer");
        assertCafFieldEquals("SELF_EMPLOYMENT_0", "Yes");
        assertCafFieldEquals("INCOME_PER_PAY_PERIOD_0", "1.00");
        assertCafFieldEquals("DATE_OF_BIRTH_0", "09/14/1950");
        assertCafFieldEquals("SSN_0", "XXX-XX-XXXX");
        assertCafFieldEquals("COUNTY_INSTRUCTIONS",
                             "This application was submitted. A caseworker at Hennepin County will help route your application to your county. For more support with your application, you can call Hennepin County at 612-596-1300.");
        assertCafFieldEquals("PROGRAMS", "SNAP, CASH, CCAP, EA, GRH");
        assertCafFieldEquals("FULL_NAME", "Ahmed St. George");
        assertCafFieldEquals("FULL_NAME_0", "householdMemberFirstName householdMemberLastName");
        assertCafFieldEquals("PROGRAMS_0", "CCAP");
        assertCafFieldEquals("SNAP_EXPEDITED_ELIGIBILITY", "");
        assertCafFieldEquals("CCAP_EXPEDITED_ELIGIBILITY", "");
        assertCafFieldEquals("GROSS_MONTHLY_INCOME_0", "120.00");
        assertCafFieldEquals("CREATED_DATE", "2021-06-14");
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
        assertCafFieldEquals("APPLICANT_MAILING_APT_NUMBER", "");
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
        assertCafFieldEquals("ROOM_AND_BOARD", "No");
        assertCafFieldEquals("RECEIVED_LIHEAP", "Yes");
        assertCafFieldEquals("REGISTER_TO_VOTE", "Yes");
        assertCafFieldEquals("SELF_EMPLOYED", "Yes");
        assertCafFieldEquals("SELF_EMPLOYED_GROSS_MONTHLY_EARNINGS", "see question 9");
        assertCafFieldEquals("PAY_FREQUENCY_0", "Hourly");
        assertCafFieldEquals("APPLICANT_HOME_APT_NUMBER", "someApartmentNumber");
        assertCafFieldEquals("APPLICANT_HOME_CITY", "someCity");
        assertCafFieldEquals("APPLICANT_HOME_STATE", "MN");
        assertCafFieldEquals("APPLICANT_HOME_ZIPCODE", "12345");
        assertCafFieldEquals("LIVING_SITUATION", "UNKNOWN");
        assertCafFieldEquals("MEDICAL_EXPENSES_SELECTION", "ONE_SELECTED");
        assertCafFieldEquals("EMPLOYEE_FULL_NAME_0", "householdMemberFirstName householdMemberLastName");
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
        assertCafFieldEquals("GOING_TO_SCHOOL", "No");
        assertCafFieldEquals("IS_PREGNANT", "Yes");
        assertCafFieldEquals("IS_US_CITIZEN", "No");
        assertCafFieldEquals("EXPEDITED_QUESTION_2", "1234");
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
        assertCafFieldEquals("ADDITIONAL_INCOME_INFO", "I also make a small amount of money from my lemonade stand.");
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
        assertCafFieldEquals("ADDITIONAL_APPLICATION_INFO", "I need you to contact my work for proof of termination");
        assertCafFieldEquals("EMPLOYERS_NAME_0", "some employer");
        assertCafFieldEquals("HOURLY_WAGE_0", "1.00");
        assertCafFieldEquals("LAST_NAME_0", "householdMemberLastName");
        assertCafFieldEquals("SEX_0", "MALE");
        assertCafFieldEquals("DATE_OF_BIRTH_0", "09/14/1950");
        assertCafFieldEquals("DATE_OF_MOVING_TO_MN_0", "02");
        assertCafFieldEquals("SSN_0", "XXX-XX-XXXX");
        assertCafFieldEquals("FIRST_NAME_0", "householdMemberFirstName");
        assertCafFieldEquals("PREVIOUS_STATE_0", "Illinois");
        assertCafFieldEquals("OTHER_NAME_0", "houseHoldyMcMemberson");
        assertCafFieldEquals("CCAP_0", "Yes");
        assertCafFieldEquals("RELATIONSHIP_0", "my child");
        assertCafFieldEquals("MARITAL_STATUS_0", "NEVER_MARRIED");
        assertCafFieldEquals("GROSS_MONTHLY_INCOME_0", "120.00");
        assertCafFieldEquals("APPLICANT_HOME_STREET_ADDRESS", "someStreetAddress (not permanent)");
        assertCafFieldEquals("MONEY_MADE_LAST_MONTH", "120.00");
    }
}
