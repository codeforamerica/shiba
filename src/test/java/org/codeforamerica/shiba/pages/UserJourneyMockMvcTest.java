package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UserJourneyMockMvcTest extends AbstractShibaMockMvcTest {

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
  void healthcareCoverageDoesNotDisplayOnSuccessPageWhenClientAlreadyHasHealthcare()
      throws Exception {
    var successPage = nonExpeditedFlowToSuccessPage(true, true, true, true);
    assertThat(successPage.getElementById("healthcareCoverage")).isNull();
  }

  @Test
  void healthcareCoverageDisplaysOnSuccessPageWhenClientDoesNotHaveHealthcare() throws Exception {
    var successPage = nonExpeditedFlowToSuccessPage(false, false, false, false);
    assertThat(successPage.getElementById("healthcareCoverage")).isNotNull();
  }

  @Test
  void userCanCompleteTheNonExpeditedHouseholdFlowWithNoEmployment() throws Exception {
    nonExpeditedFlowToSuccessPage(true, false);
  }

  @Test
  void userCanCompleteTheExpeditedFlowWithoutBeingExpedited() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("SNAP", "CCAP");

    FormPage reviewInfoPage = new FormPage(getPage("reviewInfo"));
    reviewInfoPage
        .assertLinkWithTextHasCorrectUrl("Submit an incomplete application now with only the above information.",
            "/pages/doYouNeedHelpImmediately");

    postExpectingRedirect("doYouNeedHelpImmediately",
        "needHelpImmediately",
        "true",
        "addHouseholdMembersExpedited");
    postExpectingRedirect("addHouseholdMembersExpedited", "addHouseholdMembers", "false",
        "expeditedIncome");
    postExpectingRedirect("expeditedIncome", "moneyMadeLast30Days", "123", "expeditedHasSavings");
    
    postExpectingRedirect("expeditedHasSavings", "haveSavings", "true", "liquidAssets");
    postExpectingRedirect("liquidAssets", "liquidAssets", "1233", "expeditedExpenses");
    postExpectingRedirect("expeditedExpenses", "payRentOrMortgage", "true",
        "expeditedExpensesAmount");
    postExpectingRedirect("expeditedExpensesAmount", "homeExpensesAmount", "333",
        "expeditedUtilityPayments");
    postExpectingRedirect("expeditedUtilityPayments", "payForUtilities", "COOLING",
        "expeditedMigrantFarmWorker");
    postExpectingRedirect("expeditedMigrantFarmWorker",
        "migrantOrSeasonalFarmWorker",
        "false",
        "snapExpeditedDetermination");
    FormPage page = new FormPage(getPage("snapExpeditedDetermination"));
    assertThat(page.getElementsByTag("p").get(0).text()).isEqualTo(
        "An eligibility worker will contact you within 5-7 days to review your application.");
    assertNavigationRedirectsToCorrectNextPage("snapExpeditedDetermination", "legalStuff");
    page = new FormPage(getPage("legalStuff"));
    assertThat(page.getTitle()).isEqualTo("Legal Stuff");
    assertThat(page.getElementById("ccap-legal")).isNotNull();
  }

  @Test
  void partialFlow() throws Exception {
    getToDocumentUploadScreen();
    completeDocumentUploadFlow();
    FormPage page = new FormPage(getPage("success"));
    assertThat(page.getLinksContainingText("Download your application")).hasSizeGreaterThan(0);
    assertThat(page.getLinksContainingText("Download your application").get(0).attr("href"))
        .isEqualTo("/download");
  }

  @Test
  void shouldHandleDeletionOfLastHouseholdMember() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("CCAP");

    // Add and delete one household member
    postExpectingSuccess("addHouseholdMembers", "addHouseholdMembers", "true");
    fillOutHousemateInfo("EA");
    deleteOnlyHouseholdMember();

    // When we "hit back", we should be redirected to reviewInfo
    getWithQueryParamAndExpectRedirect("householdDeleteWarningPage", "iterationIndex", "0",
        "reviewInfo");
  }

  @Test
  void shouldValidateContactInfoEmailEvenIfEmailNotSelected() throws Exception {
    completeFlowFromLandingPageThroughContactInfo("CCAP");
    // Submitting an invalid email address should keep you on the same page
    postExpectingFailure("contactInfo", Map.of(
        "phoneNumber", List.of("7234567890"),
        "email", List.of("example.com"),
        "phoneOrEmail", List.of("TEXT")
    ));
  }

  @Test
  void shouldNotShowValidationWarningWhenPressingBackOnFormWithNotEmptyValidationCondition()
      throws Exception {
    getToPersonalInfoScreen("CCAP");
    postExpectingSuccess("personalInfo", Map.of(
        "firstName", List.of("defaultFirstName"),
        "lastName", List.of("defaultLastName"),
        "dateOfBirth", List.of("01", "12", "1928")
    ));
    assertFalse(new FormPage(getPage("personalInfo")).hasInputError());
  }

  @Test
  void shouldNotAllowNonPrintableUnicode() throws Exception {
    getToPersonalInfoScreen("CCAP");
    postExpectingSuccess("personalInfo", Map.of(
        // unicode null
        "firstName", List.of("Amanda" + "\u0000"),
        // \n is another unicode ctrl character, but that one we want to keep
        "lastName", List.of("Sm\nith"),
        "dateOfBirth", List.of("01", "12", "1928")
    ));

    // remove the null character
    String firstName = applicationData.getPagesData()
        .safeGetPageInputValue("personalInfo", "firstName").get(0);
    assertThat(firstName).isEqualTo("Amanda");

    // We should keep the \n character
    String lastName = applicationData.getPagesData()
        .safeGetPageInputValue("personalInfo", "lastName").get(0);
    assertThat(lastName).isEqualTo("Sm\nith");
  }

  /**
   * This test verifies that we save the option that the user selected on the outOfStateAddressNotice page
   * @throws Exception
   */
  @Test
  void shouldSaveSelectedOutOfStateAddressOption() throws Exception {
	getToOutOfStateAddressNotice("CASH");
	
    postExpectingSuccess("outOfStateAddressNotice", Map.of("selectedOutOfStateAddressOption", List.of("CONTINUE")));
    String selectedOption = applicationData.getPagesData()
        .safeGetPageInputValue("outOfStateAddressNotice", "selectedOutOfStateAddressOption").get(0);
    assertThat(selectedOption).isEqualTo("CONTINUE");

    postExpectingSuccess("outOfStateAddressNotice", Map.of("selectedOutOfStateAddressOption", List.of("EDIT")));
    selectedOption = applicationData.getPagesData()
        .safeGetPageInputValue("outOfStateAddressNotice", "selectedOutOfStateAddressOption").get(0);
    assertThat(selectedOption).isEqualTo("EDIT");

    postExpectingSuccess("outOfStateAddressNotice", Map.of("selectedOutOfStateAddressOption", List.of("QUIT")));
    selectedOption = applicationData.getPagesData()
        .safeGetPageInputValue("outOfStateAddressNotice", "selectedOutOfStateAddressOption").get(0);
    assertThat(selectedOption).isEqualTo("QUIT");
  }

  /**
   * These test cases verify the page navigation within the Personal Details
   * section of MNbenefits. Test cases are limited to applications with a single
   * program selection.
   * 
   * @param program  - a single program
   * @param addChild - when "false" the test runs as an applicant-only
   *                 application, when "true" the test run with one child added to
   *                 the household but with "None" selected for the child's
   *                 program selection
   * @throws Exception
   */
  @ParameterizedTest
  @CsvSource(value = { "SNAP, false", "SNAP, true", "CASH, false", "CASH, true", "EA, false", "EA, true",
		  "GRH, false", "GRH, true", "CCAP, false", "CCAP, true", "CERTAIN_POPS, false", "CERTAIN_POPS, true" })
  void shouldNavigatePersonalDetailsFlow(String program, String addChild) throws Exception {
	  String[] programs = { program };

	  // Use Chisago County to enable Certain Pops.
	  postExpectingSuccess("identifyCountyBeforeApplying", "county", List.of("Chisago"));

	  // navigation from choosePrograms to introBasicInfo
	  switch (program) {
		  case "SNAP": {
			  postExpectingRedirect("choosePrograms", "programs", Arrays.stream(programs).toList(), "expeditedNotice");
			  assertNavigationRedirectsToCorrectNextPage("expeditedNotice", "introBasicInfo");
			  break;
		  }
		  case "CERTAIN_POPS": {
			  postExpectingRedirect("choosePrograms", "programs", Arrays.stream(programs).toList(), "basicCriteria");
			  postExpectingRedirect("basicCriteria", "basicCriteria", "SIXTY_FIVE_OR_OLDER", "certainPopsConfirm");
			  assertNavigationRedirectsToCorrectNextPage("certainPopsConfirm", "introBasicInfo");
			  break;
		  }
		  default: {
			  postExpectingRedirect("choosePrograms", "programs", Arrays.stream(programs).toList(), "introBasicInfo");
		  }
	  }

	  fillInPersonalInfoAndContactInfoAndAddress();

	  // navigation from addHouseholdMembers to housingSubsidy
	  switch (addChild) {
		  case "false": { // applicant-only case
			  switch (program) {
				  case "CCAP": {
					  postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false", "addChildrenConfirmation");
					  assertNavigationRedirectsToCorrectNextPageWithOption("addChildrenConfirmation", "false",
							  "introPersonalDetails");
					  break;
				  }
				  default: {
					  postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false", "introPersonalDetails");
					  break;
				  }
			  }
			  switch (program) {
				  case "CERTAIN_POPS": {
					  // will not navigate to housingSubsidy when only progam is Certain Pops
					  assertNavigationRedirectsToCorrectNextPage("introPersonalDetails", "livingSituation");
					  break;
				  }
				  default: {
					  assertNavigationRedirectsToCorrectNextPage("introPersonalDetails", "housingSubsidy");
				  }
			  }
		  }
		  default: { // applicant with one child in household case
			  postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
			  assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
			  Map<String, List<String>> householdMemberInfo = new HashMap<>();
			  householdMemberInfo.put("firstName", List.of("childFirstName"));
			  householdMemberInfo.put("lastName", List.of("childLastName"));
			  householdMemberInfo.put("programs", List.of("None"));
			  householdMemberInfo.put("relationship", List.of("child"));
			  householdMemberInfo.put("dateOfBirth", List.of("09", "14", "2000"));
			  householdMemberInfo.put("ssn", List.of("987654321"));
			  householdMemberInfo.put("maritalStatus", List.of("Never married"));
			  householdMemberInfo.put("sex", List.of("Male"));
			  householdMemberInfo.put("livedInMnWholeLife", List.of("Yes"));
			  postExpectingRedirect("householdMemberInfo", householdMemberInfo, "householdList");
	
			  // The flow to the introPersonalDetails page varies based on program selection
			  switch (program) {
				  case "CCAP": {
					  assertNavigationRedirectsToCorrectNextPage("householdList", "childrenInNeedOfCare");
					  postExpectingRedirect("childrenInNeedOfCare", "whoNeedsChildCare", "childFirstName childLastName",
							  "whoHasParentNotAtHome");
					  postExpectingRedirect("whoHasParentNotAtHome", "whoHasAParentNotLivingAtHome", "NONE_OF_THE_ABOVE",
							  "housingSubsidy");
					  break;
				  }
				  case "SNAP": {
					  assertNavigationRedirectsToCorrectNextPage("householdList", "preparingMealsTogether");
					  postExpectingRedirect("preparingMealsTogether", "preparingMealsTogether", "true", "housingSubsidy");
					  break;
				  }
				  case "CERTAIN_POPS": {
					  assertNavigationRedirectsToCorrectNextPage("householdList", "livingSituation");
					  break;
				  }
				  default: {
					  assertNavigationRedirectsToCorrectNextPage("householdList", "housingSubsidy");
					  break;
				  }
			  }
		  }
	  }

	  // navigation from housingSubsidy to goingToSchool
	  switch (program) {
		  case "GRH", "CCAP": {
			  postExpectingRedirect("housingSubsidy", "hasHousingSubsidy", "false", "livingSituation");
			  postExpectingRedirect("livingSituation", "livingSituation",
					  "PAYING_FOR_HOUSING_WITH_RENT_LEASE_OR_MORTGAGE", "goingToSchool");
			  break;
		  }
		  case "CERTAIN_POPS": {
			  postExpectingRedirect("livingSituation", "livingSituation",
					  "PAYING_FOR_HOUSING_WITH_RENT_LEASE_OR_MORTGAGE", "goingToSchool");
			  break;
		  }
		  default: {
			  postExpectingRedirect("housingSubsidy", "hasHousingSubsidy", "false", "goingToSchool");
		  }
	  }

	  // navigation from goingToSchool to introIncome
	  postExpectingRedirect("goingToSchool", "goingToSchool", "false", "pregnant");
	  postExpectingRedirect("pregnant", "isPregnant", "false", "migrantFarmWorker");
	  postExpectingRedirect("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false", "usCitizen");
	  postExpectingRedirect("usCitizen", "isUsCitizen", "true", "disability");
	  postExpectingRedirect("disability", "hasDisability", "false", "workSituation");
	  postExpectingRedirect("workSituation", "hasWorkSituation", "false", "tribalNationMember");
	  postExpectingRedirect("tribalNationMember", "isTribalNationMember", "false", "introIncome");
  }

  protected void completeFlowFromReviewInfoToDisability(String... applicantPrograms)
      throws Exception {
    postExpectingSuccess("addHouseholdMembers", "addHouseholdMembers", "false");
    if (Arrays.stream(applicantPrograms)
        .anyMatch(program -> program.equals("CCAP") || program.equals("GRH"))) {
      assertNavigationRedirectsToCorrectNextPage("addHouseholdMembers", "introPersonalDetails");
      assertNavigationRedirectsToCorrectNextPage("introPersonalDetails", "livingSituation");
      postExpectingRedirect("livingSituation", "livingSituation", "UNKNOWN", "goingToSchool");
    } else {
      assertNavigationRedirectsToCorrectNextPage("addHouseholdMembers", "goingToSchool");
    }

    postExpectingRedirect("goingToSchool", "goingToSchool", "true", "pregnant");
    postExpectingRedirect("pregnant", "isPregnant", "false", "migrantFarmWorker");
    postExpectingRedirect("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false", "usCitizen");
    postExpectingRedirect("usCitizen", "isUsCitizen", "true", "disability");
    postExpectingRedirect("disability", "hasDisability", "false", "workSituation");
  }
}
