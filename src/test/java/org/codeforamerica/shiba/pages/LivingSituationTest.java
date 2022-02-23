package org.codeforamerica.shiba.pages;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LivingSituationTest extends AbstractShibaMockMvcTest {

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
  void shouldAskLivingSituationIfCCAPApplicant() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("CCAP");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "livingSituation");
  }

  @Test
  void shouldAskLivingSituationIfGRHApplicant() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("GRH");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    postExpectingRedirect("startHousehold", "livingSituation");
    fillOutHousemateInfo("EA");
    finishAddingHouseholdMembers("goingToSchool");
  }

  @Test
  void shouldAskLivingSituationIfGRHApplicantLivingAlone() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("GRH");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false",
        "introPersonalDetails");
    assertNavigationRedirectsToCorrectNextPage("introPersonalDetails", "livingSituation");
  }

  @Test
  void shouldAskLivingSituationIfCCAPHouseholdMember() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("EA");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "livingSituation");
  }

  @Test
  void shouldNotAskLivingSituationIfNotCCAPorGRH() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("EA");
    postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
    fillOutHousemateInfo("EA");
    finishAddingHouseholdMembers("goingToSchool");
  }
}
