package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.assertPdfFieldEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserJourneyMockMvcTest extends AbstractShibaMockMvcTest {

  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
    mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
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
        .assertLinkWithTextHasCorrectUrl("Submit application now with only the above information.",
            "/pages/doYouNeedHelpImmediately");

    getNavigationPageWithQueryParamAndExpectRedirect("doYouNeedHelpImmediately",
        "option",
        "0",
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
        "A caseworker will contact you within 5-7 days to review your application.");
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
    assertThat(page.getLinksContainingText("Emergency Application")).hasSizeGreaterThan(0);
    assertThat(page.getLinksContainingText("Emergency Application").get(0).attr("href"))
        .isEqualTo("/download");

    PDAcroForm caf = this.downloadCaf();
    assertPdfFieldEquals("APPLICANT_WRITTEN_LANGUAGE_PREFERENCE", "ENGLISH", caf);
    assertPdfFieldEquals("APPLICANT_SPOKEN_LANGUAGE_PREFERENCE", "ENGLISH", caf);
    assertPdfFieldEquals("NEED_INTERPRETER", "Off", caf);
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
  void shouldSkipDocumentUploadFlowIfNotApplicableRegardlessOfPrograms() throws Exception {
    var applicantPrograms = new String[]{"SNAP", "CASH", "CCAP", "EA", "GRH"};
    completeFlowFromLandingPageThroughReviewInfo(applicantPrograms);
    completeFlowFromReviewInfoToDisability(applicantPrograms);

    postExpectingRedirect("workSituation", "hasWorkSituation", "false", "introIncome");
    assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
    postExpectingNextPageTitle("employmentStatus", "areYouWorking", "false", "Job Search");
    postExpectingRedirect("jobSearch", "currentlyLookingForJob", "false", "incomeUpNext");

    assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
    postExpectingRedirect("unearnedIncome", "unearnedIncome", "NO_UNEARNED_INCOME_SELECTED",
        "unearnedIncomeCcap");
    var formPage = postAndFollowRedirect(
        "unearnedIncomeCcap", "unearnedIncomeCcap", "NO_UNEARNED_INCOME_CCAP_SELECTED");
    assertEquals(formPage.getTitle(), "Additional Income Info");

    postExpectingRedirect("additionalIncomeInfo",
        "additionalIncomeInfo",
        "my income is literally $0",
        "startExpenses");
    assertNavigationRedirectsToCorrectNextPage("startExpenses", "homeExpenses");
    postExpectingRedirect("homeExpenses", "homeExpenses", "NONE_OF_THE_ABOVE", "utilities");

    // skipping ahead to medical expenses
    postExpectingRedirect("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE",
        "supportAndCare");

    // skipping ahead to signing application
    submitApplication();
    assertNavigationRedirectsToCorrectNextPage("signThisApplication", "nextSteps");
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
