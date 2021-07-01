package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
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
//        testPage.clickLink("This looks correct");
//        testPage.enter("addHouseholdMembers", NO.getDisplayValue());
//        testPage.clickContinue();
//        testPage.clickContinue();
//        testPage.enter("goingToSchool", YES.getDisplayValue());
//        testPage.enter("isPregnant", YES.getDisplayValue());
//        testPage.enter("migrantOrSeasonalFarmWorker", YES.getDisplayValue());
//        testPage.enter("isUsCitizen", YES.getDisplayValue());
//        testPage.enter("hasDisability", YES.getDisplayValue());
//        testPage.enter("hasWorkSituation", YES.getDisplayValue());
//        testPage.clickContinue();
//        testPage.enter("areYouWorking", NO.getDisplayValue());
//        assertThat(driver.getTitle()).isEqualTo("Job Search");
//        testPage.enter("currentlyLookingForJob", YES.getDisplayValue());
//        assertThat(driver.getTitle()).isEqualTo("Income Up Next");
//        fillUnearnedIncomeToLegalStuffCCAP();
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
        fillOutMailingAddress(locationClient);
        postExpectingSuccessAndAssertRedirectPageElementHasText("verifyMailingAddress", "useEnrichedAddress", "true",
                                                                "mailingAddress-address_street", "smarty street");
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

    protected void fillOutMailingAddress(LocationClient locationClient) throws Exception {
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
