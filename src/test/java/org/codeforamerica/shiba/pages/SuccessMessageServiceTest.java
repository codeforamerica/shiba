package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.testutilities.AbstractPageControllerTest;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpSession;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SuccessMessageServiceTest extends AbstractPageControllerTest {
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

    @SuppressWarnings("unused")
    private static Stream<Arguments> successMessageTestCases() {
        return Stream.of(
                Arguments.of(
                        "Only Expedited SNAP",
                        List.of("SNAP"),
                        SnapExpeditedEligibility.ELIGIBLE,
                        CcapExpeditedEligibility.UNDETERMINED,
                        "Within 24 hours, <strong>expect a call</strong> from your county about your food assistance application."
                ),
                Arguments.of(
                        "Only Non-expedited SNAP",
                        List.of("SNAP"),
                        SnapExpeditedEligibility.NOT_ELIGIBLE,
                        CcapExpeditedEligibility.UNDETERMINED,
                        "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your food support application. The letter will explain your next steps."
                ),
                Arguments.of(
                        "Expedited SNAP + Expedited CCAP",
                        List.of("SNAP", "CCAP"),
                        SnapExpeditedEligibility.ELIGIBLE,
                        CcapExpeditedEligibility.ELIGIBLE,
                        "Within 5 days, your county will determine your childcare assistance case and <strong>send you a letter in the mail</strong>.</p>"
                ),
                Arguments.of(
                        "Expedited SNAP + non-expedited CCAP",
                        List.of("SNAP", "CCAP"),
                        SnapExpeditedEligibility.ELIGIBLE,
                        CcapExpeditedEligibility.NOT_ELIGIBLE,
                        "Within 24 hours, <strong>expect a call</strong> from your county about your food assistance application."
                ),
                Arguments.of(
                        "Expedited CCAP + non-expedited SNAP",
                        List.of("SNAP", "CCAP"),
                        SnapExpeditedEligibility.NOT_ELIGIBLE,
                        CcapExpeditedEligibility.ELIGIBLE,
                        "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your food support application. The letter will explain your next steps."
                ),
                Arguments.of(
                        "Only Expedited CCAP",
                        List.of("CCAP"),
                        SnapExpeditedEligibility.UNDETERMINED,
                        CcapExpeditedEligibility.ELIGIBLE,
                        "Within 5 days, your county will determine your childcare assistance case and <strong>send you a letter in the mail</strong>.</p>"
                ),
                Arguments.of(
                        "Only Non-expedited CCAP",
                        List.of("CCAP"),
                        SnapExpeditedEligibility.UNDETERMINED,
                        CcapExpeditedEligibility.NOT_ELIGIBLE,
                        "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your childcare application. The letter will explain your next steps.</p>"
                ),
                Arguments.of(
                        "Expedited SNAP + any other program",
                        List.of("SNAP", "GRH"),
                        SnapExpeditedEligibility.ELIGIBLE,
                        CcapExpeditedEligibility.UNDETERMINED,
                        "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your housing application. The letter will explain your next steps.</p>"
                ),
                Arguments.of(
                        "Expedited SNAP + multiple other programs",
                        List.of("SNAP", "GRH", "EA"),
                        SnapExpeditedEligibility.ELIGIBLE,
                        CcapExpeditedEligibility.UNDETERMINED,
                        "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your housing and emergency assistance application. The letter will explain your next steps.</p>"
                ),
                Arguments.of(
                        "Expedited CCAP + any other program besides SNAP",
                        List.of("CCAP", "GRH"),
                        SnapExpeditedEligibility.UNDETERMINED,
                        CcapExpeditedEligibility.ELIGIBLE,
                        "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your housing application. The letter will explain your next steps.</p>"
                ),
                Arguments.of(
                        "Non-expedited CCAP + any other program besides SNAP",
                        List.of("CCAP", "GRH"),
                        SnapExpeditedEligibility.UNDETERMINED,
                        CcapExpeditedEligibility.NOT_ELIGIBLE,
                        "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your childcare and housing application. The letter will explain your next steps.</p>"
                )

        );
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "{0}")
    @MethodSource("org.codeforamerica.shiba.pages.SuccessMessageServiceTest#successMessageTestCases")
    void displaysCorrectSuccessMessageForApplicantPrograms(String testName, List<String> programs, SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility, String expectedMessage) throws Exception {
        setPrograms(programs);

        setSubworkflows(new Subworkflows());

        assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessage);
    }

    @Test
    void displaysCorrectSuccessMessageForHouseholdMemberPrograms() throws Exception {
        setPrograms(List.of("SNAP"));

        setSubworkflows(new Subworkflows(Map.of("household", new Subworkflow(List.of(
                new PagesDataBuilder().build(List.of(new PageDataBuilder("householdMemberInfo", Map.of("programs", List.of("GRH", "EA")))))
        )))));

        var snapExpeditedEligibility = SnapExpeditedEligibility.ELIGIBLE;
        var ccapExpeditedEligibility = CcapExpeditedEligibility.UNDETERMINED;
        var expectedMessage = "Within 24 hours, <strong>expect a call</strong> from your county about your food assistance application.</p>";
        assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessage);
    }

    private void setSubworkflows(Subworkflows subworkflows) {
        applicationData.setSubworkflows(subworkflows);
    }

    private void setPrograms(List<String> programs) {
        applicationData.setPagesData(new PagesDataBuilder().build(
                List.of(new PageDataBuilder("choosePrograms", Map.of("programs", programs))))
        );
    }

    private void assertCorrectMessage(SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility, String expectedMessage) throws Exception {
        when(snapExpeditedEligibilityDecider.decide(any())).thenReturn(snapExpeditedEligibility);
        when(ccapExpeditedEligibilityDecider.decide(any())).thenReturn(ccapExpeditedEligibility);
        mockMvc.perform(get("/pages/nextSteps").session(new MockHttpSession()))
                .andExpect(status().isOk())
                //.andExpect(model().attribute("successMessage", expectedMessage)) // assert the message is right
                .andExpect(content().string(containsString(expectedMessage)));
    }
}
