package org.codeforamerica.shiba;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ccap")
public class CCAPMockMvcTest extends AbstractShibaMockMvcTest {

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    mockMvc.perform(get("/pages/identifyCountyBeforeApplying").session(session)); // start timer
    postExpectingSuccess("identifyCountyBeforeApplying", "county", "Hennepin");
    postExpectingSuccess("languagePreferences",
        Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH"))
    );
  }

  @Test
  void verifyFlowWhenNoOneHasSelectedCCAPInHousehold() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("SNAP");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
    fillOutHousemateInfo("EA");
    finishAddingHouseholdMembers("preparingMealsTogether");
    postExpectingNextPageTitle("preparingMealsTogether", "isPreparingMealsTogether", "false",
        "Going to school");
    postExpectingNextPageTitle("goingToSchool", "goingToSchool", "true", "Pregnant");
    completeFlowFromIsPregnantThroughTribalNations(true, "SNAP");
    assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
    postExpectingNextPageTitle("employmentStatus", "areYouWorking", "false", "Income Up Next");
    assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
    postExpectingRedirect("unearnedIncome", "unearnedIncome", "NO_UNEARNED_INCOME_SELECTED",
        "futureIncome");
    fillAdditionalIncomeInfo("SNAP");
    postExpectingRedirect("supportAndCare", "supportAndCare", "false", "assets");
    postExpectingSuccess("assets", "assets", "VEHICLE");
    assertNavigationRedirectsToCorrectNextPage("assets", "savings");
    postExpectingRedirect("savings", "haveSavings", "true", "liquidAssetsSingle");
    postExpectingNextPageTitle("liquidAssetsSingle", "liquidAssets", "1234", "Sold assets");
    assertPageDoesNotHaveElementWithId("legalStuff", "ccap-legal");
  }

  @Test
  void verifyFlowWhenLiveAloneApplicantHasNotSelectedCCAP() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("SNAP");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false",
        "introPersonalDetails");
    postExpectingRedirect("livingSituation", "goingToSchool");
    postExpectingRedirect("goingToSchool", "goingToSchool", "true", "pregnant");
    completeFlowFromIsPregnantThroughTribalNations(false, "SNAP");
    assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
    postExpectingNextPageTitle("employmentStatus", "areYouWorking", "false", "Income Up Next");
    assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
    postExpectingRedirect("unearnedIncome", "unearnedIncome", "NO_UNEARNED_INCOME_SELECTED",
        "futureIncome");
    fillAdditionalIncomeInfo("SNAP");
    postExpectingRedirect("supportAndCare", "supportAndCare", "false", "assets");
    postExpectingSuccess("assets", "assets", "VEHICLE");
    assertNavigationRedirectsToCorrectNextPage("assets", "savings");
    postExpectingRedirect("savings", "haveSavings", "true", "liquidAssetsSingle");
    postExpectingNextPageTitle("liquidAssetsSingle", "liquidAssets", "1234", "Sold assets");
    assertPageDoesNotHaveElementWithId("legalStuff", "ccap-legal");
  }

  @Test
  void verifyDrugFelonyQuestionIsDisplayedWhenAnythingOtherThanCCAPOnlyIsSelected() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("CCAP", "SNAP");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true",
            "startHousehold");
    fillOutHousemateInfo("EA");
    assertPageHasElementWithId("legalStuff", "drugFelony1");
  }

  @Test
  void verifyDrugFelonyQuestionIsNotDisplayedWhenCCAPOnlyIsSelected() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("CCAP");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true",
            "startHousehold");
    fillOutHousemateInfo("EA");
    assertPageDoesNotHaveElementWithId("legalStuff", "drugFelony1");
  }

  @Test
  void verifyFlowWhenLiveAloneApplicantSelectedCCAP() throws Exception {
    // Applicant lives alone and choose CCAP
    completeFlowFromLandingPageThroughReviewInfo("CCAP");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false",
        "addChildrenConfirmation");
    assertNavigationRedirectsToCorrectNextPageWithOption("addChildrenConfirmation","false","introPersonalDetails");
    postExpectingRedirect("introPersonalDetails", "livingSituation");
    postExpectingRedirect("livingSituation", "goingToSchool");
    postExpectingRedirect("goingToSchool", "goingToSchool", "true", "pregnant");
    postExpectingRedirect("pregnant", "isPregnant", "true", "migrantFarmWorker");
    postExpectingRedirect("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "true", "usCitizen");
    postExpectingRedirect("usCitizen", "isUsCitizen", "true", "workSituation");
    postExpectingRedirect("workSituation", "hasWorkSituation", "false", "tribalNationMember");
    postExpectingRedirect("tribalNationMember", "isTribalNationMember", "false", "introIncome");
    assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
    postExpectingRedirect("employmentStatus", "areYouWorking", "false", "jobSearch");
    postExpectingRedirect("jobSearch", "currentlyLookingForJob", "true", "incomeUpNext");
    fillUnearnedIncomeToLegalStuffCCAP("CCAP");
  }

  @Test
  void verifyFlowWhenApplicantSelectedCCAPAndHouseholdMemberDidNot() throws Exception {
    // Applicant selected CCAP for themselves and did not choose CCAP for household member
    completeFlowFromLandingPageThroughReviewInfo("CCAP");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
    fillOutHousemateInfo("EA");
    finishAddingHouseholdMembers("childrenInNeedOfCare");
    assertCorrectPageTitle("childrenInNeedOfCare", "Who are the children in need of care?");
    postExpectingRedirect("childrenInNeedOfCare", "livingSituation");
    postExpectingRedirect("livingSituation", "goingToSchool");
    postExpectingNextPageTitle("goingToSchool", "goingToSchool", "true", "Who is going to school?");
    completeFlowFromIsPregnantThroughTribalNations(true, "CCAP", "EA");
    assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
    postExpectingNextPageTitle("employmentStatus", "areYouWorking", "false", "Job Search");
    postExpectingNextPageTitle("jobSearch", "currentlyLookingForJob", "true",
        "Who is looking for a job");
    fillUnearnedIncomeToLegalStuffCCAP("CCAP", "EA");
  }

  @Test
  void verifyFlowWhenOnlyHouseholdMemberSelectedCCAP() throws Exception {
    selectPrograms("NONE");
    assertPageHasWarningMessage("choosePrograms",
        "You will be asked to share some information about yourself even though you're only applying for others.");

    completeFlowFromLandingPageThroughReviewInfo("NONE");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
    fillOutHousemateInfo("CCAP");

    // Don't select any children in need of care, should get redirected to preparing meals together
    assertCorrectPageTitle("childrenInNeedOfCare", "Who are the children in need of care?");
    postExpectingNextPageTitle("childrenInNeedOfCare", "Living situation");

    // Go back to childrenInNeedOfCare and select someone this time, but don't select anyone having a parent not at home
    String householdMemberId = getFirstHouseholdMemberId();
    postExpectingNextPageTitle("childrenInNeedOfCare",
        "whoNeedsChildCare",
        List.of("defaultFirstName defaultLastName applicant",
            "householdMemberFirstName householdMemberLastName" + householdMemberId),
        "Who are the children that have a parent not living in the home?"
    );
    postExpectingNextPageTitle("whoHasParentNotAtHome",
        "whoHasAParentNotLivingAtHome",
        List.of("NONE_OF_THE_ABOVE"),
        "Living situation");

    // Go back and select someone having a parent not at home
    postExpectingNextPageTitle("whoHasParentNotAtHome",
        "whoHasAParentNotLivingAtHome",
        List.of("defaultFirstName defaultLastName applicant"),
        "Name of parent outside home");
    postExpectingNextPageTitle("parentNotAtHomeNames",
        Map.of("whatAreTheParentsNames", List.of("My Parent", "Default's Parent"),
            "childIdMap", List.of("applicant", householdMemberId)
        ),
        "Living situation");

    postExpectingRedirect("preparingMealsTogether", "isPreparingMealsTogether", "false",
        "livingSituation");
    postExpectingRedirect("livingSituation", "livingSituation", "UNKNOWN", "goingToSchool");
    postExpectingNextPageTitle("goingToSchool", "goingToSchool", "true", "Who is going to school?");
    postExpectingRedirect("whoIsGoingToSchool", "pregnant"); // no one is going to school
    completeFlowFromIsPregnantThroughTribalNations(true, "CCAP", "NONE");
    assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
    postExpectingNextPageTitle("employmentStatus", "areYouWorking", "false", "Job Search");
    postExpectingNextPageTitle("jobSearch", "currentlyLookingForJob", "true",
        "Who is looking for a job");
    fillUnearnedIncomeToLegalStuffCCAP("CCAP", "NONE");
  }

  private void fillUnearnedIncomeToLegalStuffCCAP(String... Programs) throws Exception {
    assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
    postExpectingRedirect("unearnedIncome", "unearnedIncome", "NO_UNEARNED_INCOME_SELECTED",
        "otherUnearnedIncome");
    postExpectingRedirect("otherUnearnedIncome",
        "otherUnearnedIncome",
        "NO_OTHER_UNEARNED_INCOME_SELECTED",
        "futureIncome");
    fillAdditionalIncomeInfo(Programs);
    postExpectingRedirect("supportAndCare", "supportAndCare", "false", "assets");
    postExpectingSuccess("assets", "assets", "NONE");
    assertNavigationRedirectsToCorrectNextPage("assets", "savings");
    postExpectingRedirect("savings", "haveSavings", "false", "soldAssets");
    // Go back and enter true for savings
    postExpectingRedirect("savings", "haveSavings", "true", "liquidAssetsSingle");
    postExpectingRedirect("liquidAssetsSingle", "liquidAssets", "1234", "soldAssets");
    assertPageHasElementWithId("legalStuff", "ccap-legal");
  }
}
