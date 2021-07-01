package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("addHouseholdMembers",
                                                               "addHouseholdMembers",
                                                               "false",
                                                               "introPersonalDetails");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("livingSituation", "goingToSchool");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("goingToSchool",
                                                               "goingToSchool",
                                                               "true",
                                                               "pregnant");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("pregnant",
                                                               "isPregnant",
                                                               "true",
                                                               "migrantFarmWorker");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("migrantFarmWorker",
                                                               "migrantOrSeasonalFarmWorker",
                                                               "true",
                                                               "usCitizen");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("disability",
                                                               "hasDisability",
                                                               "true",
                                                               "workSituation");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("workSituation",
                                                               "hasWorkSituation",
                                                               "true",
                                                               "introIncome");
        assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("employmentStatus",
                                                               "areYouWorking",
                                                               "false",
                                                               "jobSearch");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("jobSearch",
                                                               "currentlyLookingForJob",
                                                               "true",
                                                               "incomeUpNext");
        fillUnearnedIncomeToLegalStuffCCAP();
    }

    private void fillUnearnedIncomeToLegalStuffCCAP() throws Exception {
        assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("unearnedIncome",
                                                               "unearnedIncome",
                                                               "NO_UNEARNED_INCOME_SELECTED",
                                                               "unearnedIncomeCcap");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("unearnedIncomeCcap",
                                                               "unearnedIncomeCcap",
                                                               "NO_UNEARNED_INCOME_CCAP_SELECTED",
                                                               "futureIncome");
        fillFutureIncomeToHaveVehicle();



        /*
        assertThat(testPage.getTitle()).isEqualTo("Real Estate");
        testPage.enter("ownRealEstate", NO.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", NO.getDisplayValue());
        assertThat(testPage.getTitle()).isEqualTo("Sold assets");
        testPage.goBack();
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        assertThat(testPage.getTitle()).isEqualTo("$1M assets");
        testPage.enter("haveMillionDollars", NO.getDisplayValue());
        navigateTo("legalStuff");
        assertThat(driver.findElement(By.id("ccap-legal"))).isNotNull();
         */
    }

    private void fillFutureIncomeToHaveVehicle() throws Exception {
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("futureIncome",
                                                               "earnLessMoneyThisMonth",
                                                               "false",
                                                               "startExpenses");
        assertNavigationRedirectsToCorrectNextPage("startExpenses", "homeExpenses");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("homeExpenses",
                                                               "homeExpenses",
                                                               "NONE_OF_THE_ABOVE",
                                                               "utilities");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("utilities",
                                                               "payForUtilities",
                                                               "NONE_OF_THE_ABOVE",
                                                               "energyAssistance");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("energyAssistance",
                                                               "energyAssistance",
                                                               "false",
                                                               "medicalExpenses");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("medicalExpenses",
                                                               "medicalExpenses",
                                                               "NONE_OF_THE_ABOVE",
                                                               "supportAndCare");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("supportAndCare",
                                                               "supportAndCare",
                                                               "false",
                                                               "vehicle");
        postExpectingSuccessAndAssertRedirectPageNameIsCorrect("vehicle",
                                                               "haveVehicle",
                                                               "false",
                                                               "realEstate");
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
        postExpectingSuccessAndAssertRedirectPageElementHasText("verifyMailingAddress",
                                                                "useEnrichedAddress",
                                                                "true",
                                                                "mailingAddress-address_street",
                                                                "smarty street");
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
