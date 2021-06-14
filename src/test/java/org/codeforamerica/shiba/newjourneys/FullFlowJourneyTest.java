package org.codeforamerica.shiba.newjourneys;

import org.codeforamerica.shiba.pages.journeys.JourneyTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.List;

import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;

public class FullFlowJourneyTest extends JourneyTest {
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
    void fullApplicationFlow() {
        List<String> programSelections = List.of(PROGRAM_SNAP, PROGRAM_CCAP, PROGRAM_EA, PROGRAM_CASH, PROGRAM_GRH);

        getToHomeAddress(dateOfBirth, email, firstName, lastName, moveDate, needsInterpreter, otherName, previousCity, sex, testPage, programSelections);




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
}
