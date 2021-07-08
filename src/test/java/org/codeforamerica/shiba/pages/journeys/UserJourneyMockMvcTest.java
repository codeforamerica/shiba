package org.codeforamerica.shiba.pages.journeys;

import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.framework.FormPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("journey")
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
    void healthcareCoverageDoesNotDisplayOnSuccessPageWhenClientAlreadyHasHealthcare() throws Exception {
        var successPage = nonExpeditedFlowToSuccessPage(true, true, true, true);
        assertThat(successPage.findElementById("healthcareCoverage")).isNull();
    }

    @Test
    void healthcareCoverageDisplaysOnSuccessPageWhenClientDoesNotHaveHealthcare() throws Exception {
        var successPage = nonExpeditedFlowToSuccessPage(false, false, false, false);
        assertThat(successPage.findElementById("healthcareCoverage")).isNotNull();
    }

    @Test
    void userCanCompleteTheNonExpeditedHouseholdFlowWithNoEmployment() throws Exception {
        nonExpeditedFlowToSuccessPage(true, false);
    }

    @Test
    void userCanCompleteTheExpeditedFlowWithoutBeingExpedited() throws Exception {
        completeFlowFromLandingPageThroughReviewInfo("SNAP", "CCAP");
        FormPage reviewInfoPage = new FormPage(getPage("reviewInfo"));
        assertThat(reviewInfoPage.findLinksByText("Submit application now with only the above information.")).hasSize(1);
        String url = reviewInfoPage.findLinksByText("Submit application now with only the above information.").get(0).attr("href");
        assertThat(url).isEqualTo("/pages/doYouNeedHelpImmediately");
        this.getWithQueryParamAndExpectRedirect("doYouNeedHelpImmediately", "option", "0", "addHouseholdMembersExpedited");
        postExpectingRedirect("addHouseholdMembersExpedited", "addHouseholdMembers", "false", "expeditedIncome");
        postExpectingRedirect("expeditedIncome", "moneyMadeLast30Days", "123", "expeditedHasSavings");
        postExpectingRedirect("expeditedHasSavings", "haveSavings", "true", "liquidAssets");
        postExpectingRedirect("liquidAssets", "liquidAssets", "1233", "expeditedExpenses");
        postExpectingRedirect("expeditedExpenses", "payRentOrMortgage", "true", "expeditedExpensesAmount");
        postExpectingRedirect("expeditedExpensesAmount", "homeExpensesAmount", "333", "expeditedUtilityPayments");
        postExpectingRedirect("expeditedUtilityPayments", "payForUtilities", "COOLING", "expeditedMigrantFarmWorker");
        postExpectingRedirect("expeditedMigrantFarmWorker", "migrantOrSeasonalFarmWorker", "false", "snapExpeditedDetermination");
        FormPage page = new FormPage(getPage("snapExpeditedDetermination"));
        assertThat(page.findElementsByTag("p").get(0).text()).isEqualTo("A caseworker will contact you within 5-7 days to review your application.");
        this.assertNavigationRedirectsToCorrectNextPage("snapExpeditedDetermination", "legalStuff");
        page = new FormPage(getPage("legalStuff"));
        assertThat(page.getTitle()).isEqualTo("Legal Stuff");
        assertThat(page.findElementById("ccap-legal")).isNotNull();
   }

}
