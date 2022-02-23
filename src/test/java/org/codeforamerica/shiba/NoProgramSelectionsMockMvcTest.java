package org.codeforamerica.shiba;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.testutilities.AbstractShibaMockMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NoProgramSelectionsMockMvcTest extends AbstractShibaMockMvcTest {

  @BeforeEach
  void setup() throws Exception {
    super.setUp();
    mockMvc.perform(get("/pages/identifyCountyBeforeApplying").session(session)); // start timer
    postExpectingSuccess("identifyCountyBeforeApplying", "county", "Hennepin");
    postExpectingSuccess("languagePreferences", Map.of(
        "writtenLanguage", List.of("ENGLISH"),
        "spokenLanguage", List.of("ENGLISH"))
    );
  }

  @Test
  void clientShouldSeeNoProgramsErrorPageWhenNoProgramsSelectedForAnyone() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("NONE");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "livingSituation");
    fillOutHousemateInfoWithNoProgramsSelected();
    finishAddingHouseholdMembers("noProgramsSelected");
  }

  @Test
  void shouldSkipNoProgramsSelectedPageIfOnlyClientChoseProgram() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("CCAP");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "livingSituation");
    fillOutHousemateInfoWithNoProgramsSelected();
    finishAddingHouseholdMembers("childrenInNeedOfCare");
  }

  @Test
  void shouldSkipNoProgramsSelectedPageIfOnlyHouseholdMemberChoseProgram() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("NONE");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "livingSituation");
    fillOutHousemateInfo("SNAP");
    finishAddingHouseholdMembers("preparingMealsTogether");
  }

  @Test
  void shouldSkipNoProgramsSelectedPageIfSecondHouseholdMemberChoseProgram() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("NONE");
    assertNavigationRedirectsToCorrectNextPage("startHousehold", "livingSituation");
    fillOutHousemateInfoWithNoProgramsSelected();
    fillOutHousemateInfo("SNAP");
    finishAddingHouseholdMembers("preparingMealsTogether");
  }
}
