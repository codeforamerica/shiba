package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("ccap")
public class CcapTest extends AbstractShibaMockMvcTest {

    @BeforeEach
    protected void setUp() throws Exception {
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postWithData("languagePreferences", Map.of(
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
}
