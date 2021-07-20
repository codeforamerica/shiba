package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class LivingSituationTest extends AbstractShibaMockMvcTest {
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postExpectingSuccess("languagePreferences", Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH")));
    }

    @Test
    void shouldAskLivingSituationIfCCAPApplicant() throws Exception {
        completeFlowFromLandingPageThroughReviewInfo("CCAP");
        postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");
        fillOutHousemateInfo("EA");
        finishAddingHouseholdMembers("childrenInNeedOfCare");
        postExpectingNextPageTitle("childrenInNeedOfCare", "Living situation");
    }
}
