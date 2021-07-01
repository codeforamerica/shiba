package org.codeforamerica.shiba;

import org.codeforamerica.shiba.framework.FormPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("ccap")
public class CCAPMockMvcTest extends AbstractShibaMockMvcTest {
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postExpectingSuccess("languagePreferences", Map.of(
                "writtenLanguage", List.of("ENGLISH"),
                "spokenLanguage", List.of("ENGLISH"))
        );
    }

    @Test
    void verifyFlowWhenLiveAloneApplicantSelectedCCAP() throws Exception {
        // Applicant lives alone and choose CCAP
        completeFlowFromLandingPageThroughReviewInfo("CCAP");
        postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false", "introPersonalDetails");
        postExpectingRedirect("livingSituation", "goingToSchool");
        postExpectingRedirect("goingToSchool", "goingToSchool", "true", "pregnant");
        postExpectingRedirect("pregnant", "isPregnant", "true", "migrantFarmWorker");
        postExpectingRedirect("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "true", "usCitizen");
        postExpectingRedirect("disability", "hasDisability", "true", "workSituation");
        postExpectingRedirect("workSituation", "hasWorkSituation", "true", "introIncome");
        assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
        postExpectingRedirect("employmentStatus", "areYouWorking", "false", "jobSearch");
        postExpectingRedirect("jobSearch", "currentlyLookingForJob", "true", "incomeUpNext");
        fillUnearnedIncomeToLegalStuffCCAP();
    }

    private void fillUnearnedIncomeToLegalStuffCCAP() throws Exception {
        assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
        postExpectingRedirect("unearnedIncome", "unearnedIncome", "NO_UNEARNED_INCOME_SELECTED", "unearnedIncomeCcap");
        postExpectingRedirect("unearnedIncomeCcap", "unearnedIncomeCcap", "NO_UNEARNED_INCOME_CCAP_SELECTED", "futureIncome");
        fillFutureIncomeToHaveVehicle();
        postExpectingRedirect("realEstate", "ownRealEstate", "false", "investments");
        postExpectingRedirect("investments", "haveInvestments", "false", "savings");
        postExpectingRedirect("savings", "haveSavings", "false", "soldAssets");
        // Go back and enter true for savings
        postExpectingRedirect("savings", "haveSavings", "true", "savingsAmount");
        postExpectingRedirect("savingsAmount", "liquidAssets", "1234", "millionDollar");
        postExpectingRedirect("millionDollar", "haveMillionDollars", "false", "soldAssets");

        var legalStuff = new FormPage(getPage("legalStuff"));
        assertThat(legalStuff.getElementById("ccap-legal")).isNotNull();
    }

    private void fillFutureIncomeToHaveVehicle() throws Exception {
        postExpectingRedirect("futureIncome", "earnLessMoneyThisMonth", "false", "startExpenses");
        assertNavigationRedirectsToCorrectNextPage("startExpenses", "homeExpenses");
        postExpectingRedirect("homeExpenses", "homeExpenses", "NONE_OF_THE_ABOVE", "utilities");
        postExpectingRedirect("utilities", "payForUtilities", "NONE_OF_THE_ABOVE", "energyAssistance");
        postExpectingRedirect("energyAssistance", "energyAssistance", "false", "medicalExpenses");
        postExpectingRedirect("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE", "supportAndCare");
        postExpectingRedirect("supportAndCare", "supportAndCare", "false", "vehicle");
        postExpectingRedirect("vehicle", "haveVehicle", "false", "realEstate");
    }

    protected void completeFlowFromLandingPageThroughReviewInfo(String... programSelections) throws Exception {
        completeFlowFromLandingPageThroughContactInfo(programSelections);
    }

    protected void completeFlowFromLandingPageThroughContactInfo(String... programSelections) throws Exception {
        getToPersonalInfoScreen(programSelections);
        fillOutPersonalInfo();
        fillOutContactInfo();
        fillOutHomeAddress();
        postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "false");
        fillOutMailingAddress();
        postExpectingNextPageElementText("verifyMailingAddress", "useEnrichedAddress", "true", "mailingAddress-address_street", "smarty street");
    }

    protected void getToPersonalInfoScreen(String... programSelections) throws Exception {
        selectPrograms(programSelections);
    }

    protected void fillOutHomeAddress() throws Exception {
        postExpectingSuccess("homeAddress", Map.of(
                "streetAddress", List.of("someStreetAddress"),
                "apartmentNumber", List.of("someApartmentNumber"),
                "city", List.of("someCity"),
                "zipCode", List.of("12345"),
                "state", List.of("MN"),
                "sameMailingAddress", List.of("false")
        ));
    }

    protected void fillOutMailingAddress() throws Exception {
        when(locationClient.validateAddress(any())).thenReturn(
                Optional.of(new Address("smarty street", "City", "CA", "03104", "", "someCounty"))
        );
        postExpectingSuccess("mailingAddress", Map.of(
                "streetAddress", List.of("someStreetAddress"),
                "apartmentNumber", List.of("someApartmentNumber"),
                "city", List.of("someCity"),
                "zipCode", List.of("12345"),
                "state", List.of("IL")
        ));
    }
}
