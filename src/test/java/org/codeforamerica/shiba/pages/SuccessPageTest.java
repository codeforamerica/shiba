package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SuccessPageTest extends AbstractPageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationData applicationData;

    @BeforeEach
    void setUp() {
        applicationData.setStartTimeOnce(Instant.now());
        var id = "some-id";
        applicationData.setId(id);
        Application application = Application.builder()
                .id(id)
                .county(County.Hennepin)
                .timeToComplete(Duration.ofSeconds(12415))
                .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .applicationData(applicationData)
                .build();
        when(applicationRepository.find(any())).thenReturn(application);
    }

    @Test
    void itShowsTheRightSuccessMessageForExpeditedSnapOnly() throws Exception {
        assertCorrectSuccessMessage(
                List.of("SNAP"),
                SnapExpeditedEligibility.ELIGIBLE,
                CcapExpeditedEligibility.UNDETERMINED,
                "You will receive a call from your county within 24 hours about your application for food support. The call may come from an unknown number.<br><br>You will need to complete an interview with a caseworker.<br><br>If you don't hear from your county within 3 days or want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">call your county.</a>");
    }

    @Test
    void itShowsTheRightSuccessMessageForNonExpeditedSnapOnly() throws Exception {
        assertCorrectSuccessMessage(
                List.of("SNAP"),
                SnapExpeditedEligibility.NOT_ELIGIBLE,
                CcapExpeditedEligibility.UNDETERMINED,
                "You will receive a letter in the mail with next steps for your application for food support in 7-10 days.<br><br>You will need to complete an interview with a caseworker.<br><br>If you want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">call your county.</a>");
    }

    @Test
    void itShowsTheRightSuccessMessageForExpeditedSnapAndExpeditedCCAP() throws Exception {
        assertCorrectSuccessMessage(
                List.of("SNAP", "CCAP"),
                SnapExpeditedEligibility.ELIGIBLE,
                CcapExpeditedEligibility.ELIGIBLE,
                "You will receive a call from your county within 24 hours about your application for food support. The call may come from an unknown number.<br><br>Your county will decide on your childcare case within the next 5 working days.<br><br>You will need to complete an interview with a caseworker.<br><br>If you want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">call your county.</a>");
    }

    @Test
    void itShowsTheRightSuccessMessageForExpeditedSnapAndNonExpeditedCCAP() throws Exception {
        assertCorrectSuccessMessage(
                List.of("SNAP", "CCAP"),
                SnapExpeditedEligibility.ELIGIBLE,
                CcapExpeditedEligibility.NOT_ELIGIBLE,
                "You will receive a call from your county within 24 hours about your application for food support. The call may come from an unknown number.<br><br>You will receive a letter in the mail with next steps for your application for childcare in 7-10 days.<br><br>You will need to complete an interview with a caseworker.<br><br>If you want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">call your county.</a>");
    }

    @Test
    void itShowsTheRightSuccessMessageForNonExpeditedSnapAndExpeditedCCAP() throws Exception {
        assertCorrectSuccessMessage(
                List.of("SNAP", "CCAP"),
                SnapExpeditedEligibility.NOT_ELIGIBLE,
                CcapExpeditedEligibility.ELIGIBLE,
                "Your county will decide on your childcare case within the next 5 working days.<br><br>You will receive a letter in the mail with next steps for your application for food support in 7-10 days.<br><br>You will need to complete an interview with a caseworker.<br><br>If you want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">call your county.</a>");
    }

    @Test
    void itShowsTheRightSuccessMessageForExpeditedCCAPOnly() throws Exception {
        assertCorrectSuccessMessage(
                List.of("CCAP"),
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.ELIGIBLE,
                "Your county will decide on your childcare case within the next 5 working days.<br><br>If you want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">call your county.</a>"
        );
    }

    @Test
    void itShowsTheRightSuccessMessageForNonExpeditedCCAPOnly() throws Exception {
        assertCorrectSuccessMessage(
                List.of("CCAP"),
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.NOT_ELIGIBLE,
                "You will receive a letter in the mail with next steps for your application for childcare in 7-10 days.<br><br>If you want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">call your county.</a>"
        );
    }

    private void assertCorrectSuccessMessage(List<String> programs, SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility, String expectedMessage) throws Exception {
        when(snapExpeditedEligibilityDecider.decide(any())).thenReturn(snapExpeditedEligibility);
        when(ccapExpeditedEligibilityDecider.decide(any())).thenReturn(ccapExpeditedEligibility);

        applicationData.setPagesData(new PagesDataBuilder().build(
                List.of(new PageDataBuilder("choosePrograms", Map.of("programs", programs))))
        );

        mockMvc.perform(get("/pages/success").session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("successMessage", expectedMessage)) // assert the message is right
                .andExpect(content().string(containsString(expectedMessage)));
    }
}
