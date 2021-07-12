package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class NoProgramSelectionsMockMvcTest extends AbstractShibaMockMvcTest {
    @BeforeEach
    void setup() throws Exception {
        super.setUp();
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postExpectingSuccess("languagePreferences", Map.of(
                "writtenLanguage", List.of("ENGLISH"),
                "spokenLanguage", List.of("ENGLISH"))
        );
    }

    @Test
    void clientShouldSeeNoProgramsErrorPageWhenNoProgramsSelectedForAnyone() throws Exception {
        completeFlowFromLandingPageThroughReviewInfo("NONE");
        assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
        fillOutHousemateInfoWithNoProgramsSelected();
        getWithQueryParamAndExpectRedirect("householdList", "option", "0", "noProgramsSelected");
    }

    @Test
    void shouldSkipNoProgramsSelectedPageIfOnlyClientChoseProgram() throws Exception {
        completeFlowFromLandingPageThroughReviewInfo("CCAP");
        assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
        fillOutHousemateInfoWithNoProgramsSelected();
        getWithQueryParamAndExpectRedirect("householdList", "option", "0", "childrenInNeedOfCare");
    }

    @Test
    void shouldSkipNoProgramsSelectedPageIfOnlyHouseholdMemberChoseProgram() throws Exception {
        completeFlowFromLandingPageThroughReviewInfo("NONE");
        assertNavigationRedirectsToCorrectNextPage("startHousehold", "householdMemberInfo");
        fillOutHousemateInfo("SNAP");
    }
}
