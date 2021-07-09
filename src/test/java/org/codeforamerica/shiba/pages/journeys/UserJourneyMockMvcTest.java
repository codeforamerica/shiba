package org.codeforamerica.shiba.pages.journeys;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.framework.FormPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.TestUtils.assertPdfFieldEquals;
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
        reviewInfoPage.assertLinkWithTextHasCorrectUrl("Submit application now with only the above information.",
                                                       "/pages/doYouNeedHelpImmediately");

        getNavigationPageWithQueryParamAndExpectRedirect("doYouNeedHelpImmediately",
                                                         "option",
                                                         "0",
                                                         "addHouseholdMembersExpedited");
        postExpectingRedirect("addHouseholdMembersExpedited", "addHouseholdMembers", "false", "expeditedIncome");
        postExpectingRedirect("expeditedIncome", "moneyMadeLast30Days", "123", "expeditedHasSavings");
        postExpectingRedirect("expeditedHasSavings", "haveSavings", "true", "liquidAssets");
        postExpectingRedirect("liquidAssets", "liquidAssets", "1233", "expeditedExpenses");
        postExpectingRedirect("expeditedExpenses", "payRentOrMortgage", "true", "expeditedExpensesAmount");
        postExpectingRedirect("expeditedExpensesAmount", "homeExpensesAmount", "333", "expeditedUtilityPayments");
        postExpectingRedirect("expeditedUtilityPayments", "payForUtilities", "COOLING", "expeditedMigrantFarmWorker");
        postExpectingRedirect("expeditedMigrantFarmWorker",
                              "migrantOrSeasonalFarmWorker",
                              "false",
                              "snapExpeditedDetermination");
        FormPage page = new FormPage(getPage("snapExpeditedDetermination"));
        assertThat(page.findElementsByTag("p").get(0).text()).isEqualTo(
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
        assertThat(page.findLinksByText("Combined Application")).hasSizeGreaterThan(0);
        assertThat(page.findLinksByText("Combined Application").get(0).attr("href")).isEqualTo("/download");

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
        getWithQueryParamAndExpectRedirect("householdDeleteWarningPage", "iterationIndex", "0", "reviewInfo");
    }
}
