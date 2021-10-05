package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.AbstractPageControllerTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

public class NextStepsContentServiceTest extends AbstractPageControllerTest {

  @SuppressWarnings("unused")
  private static Stream<Arguments> successMessageTestCases() {
    return Stream.of(
        Arguments.of(
            "Only Expedited SNAP",
            List.of("SNAP"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "Within 24 hours, expect a call from your county or tribal servicing agency about your food assistance application.",
                "If you don't hear from your county or tribal servicing agency within 3 days or want an update on your case, please call your county or tribal servicing agency.")
        ),
        Arguments.of(
            "Only Non-expedited SNAP",
            List.of("SNAP"),
            SnapExpeditedEligibility.NOT_ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your food support application. The letter will explain your next steps.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited SNAP + Expedited CCAP",
            List.of("SNAP", "CCAP"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 24 hours, expect a call from your county or tribal servicing agency about your food assistance application.",
                "Within 5 days, your county or tribal servicing agency will determine your childcare assistance case and send you a letter in the mail.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited SNAP + non-expedited CCAP",
            List.of("SNAP", "CCAP"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of(
                "Within 24 hours, expect a call from your county or tribal servicing agency about your food assistance application.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your childcare application. The letter will explain your next steps.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited CCAP + non-expedited SNAP",
            List.of("SNAP", "CCAP"),
            SnapExpeditedEligibility.NOT_ELIGIBLE,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 5 days, your county or tribal servicing agency will determine your childcare assistance case and send you a letter in the mail.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your food support application. The letter will explain your next steps.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Only Expedited CCAP",
            List.of("CCAP"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 5 days, your county or tribal servicing agency will determine your childcare assistance case and send you a letter in the mail.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Only Non-expedited CCAP",
            List.of("CCAP"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of(
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your childcare application. The letter will explain your next steps.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited SNAP + any other program",
            List.of("SNAP", "GRH"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "Within 24 hours, expect a call from your county or tribal servicing agency about your food assistance application.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your housing application. The letter will explain your next steps.",
                "If you don't hear from your county or tribal servicing agency within 3 days or want an update on your case, please call your county or tribal servicing agency.")
        ),
        Arguments.of(
            "Expedited SNAP + multiple other programs",
            List.of("SNAP", "GRH", "EA"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "Within 24 hours, expect a call from your county or tribal servicing agency about your food assistance application.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your housing and emergency assistance application. The letter will explain your next steps.",
                "If you don't hear from your county or tribal servicing agency within 3 days or want an update on your case, please call your county or tribal servicing agency.")
        ),
        Arguments.of(
            "Expedited CCAP + any other program besides SNAP",
            List.of("CCAP", "GRH"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 5 days, your county or tribal servicing agency will determine your childcare assistance case and send you a letter in the mail.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your housing application. The letter will explain your next steps.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Non-expedited CCAP + any other program besides SNAP",
            List.of("CCAP", "GRH"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of(
                "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your childcare and housing application. The letter will explain your next steps.",
                "Call your county or tribal servicing agency if you don’t hear from them in the time period we’ve noted.")
        )
    );
  }

  @BeforeEach
  void setUp() {
    applicationData.setStartTimeOnce(Instant.now());
    var id = "some-id";
    applicationData.setId(id);
    PageData pageData = new PageData();
    pageData.put("email", InputData.builder().value(List.of("test@example.com")).build());
    applicationData.getPagesData().put("contactInfo", pageData);

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
  @ParameterizedTest(name = "{0}")
  @MethodSource("org.codeforamerica.shiba.pages.NextStepsContentServiceTest#successMessageTestCases")
  void displaysCorrectSuccessMessageForApplicantPrograms(String testName, List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility, List<String> expectedMessages)
      throws Exception {
    setPrograms(programs);

    setSubworkflows(new Subworkflows());

    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }

  @Test
  void displaysCorrectSuccessMessageForHouseholdMemberPrograms() throws Exception {
    setPrograms(List.of("SNAP"));

    setSubworkflows(new Subworkflows(Map.of("household", new Subworkflow(List.of(
        new PagesDataBuilder().build(List.of(
            new PageDataBuilder("householdMemberInfo", Map.of("programs", List.of("GRH", "EA")))))
    )))));

    var snapExpeditedEligibility = SnapExpeditedEligibility.ELIGIBLE;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.UNDETERMINED;
    List<String> expectedMessages = List.of(
        "Within 24 hours, expect a call from your county or tribal servicing agency about your food assistance application.",
        "In the next 7-10 days, expect to get a letter in the mail from your county or tribal servicing agency about your housing and emergency assistance application. The letter will explain your next steps.",
        "If you don't hear from your county or tribal servicing agency within 3 days or want an update on your case, please call your county or tribal servicing agency.");
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }

  private void setSubworkflows(Subworkflows subworkflows) {
    applicationData.setSubworkflows(subworkflows);
  }

  private void setPrograms(List<String> programs) {
    PageData programsPage = new PageData();
    programsPage.put("programs", InputData.builder().value(programs).build());
    applicationData.getPagesData().put("choosePrograms", programsPage);
  }

  private void assertCorrectMessage(SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility, List<String> expectedMessages)
      throws Exception {
    when(snapExpeditedEligibilityDecider.decide(any())).thenReturn(snapExpeditedEligibility);
    when(ccapExpeditedEligibilityDecider.decide(any())).thenReturn(ccapExpeditedEligibility);

    ResultActions resultActions = mockMvc.perform(
            get("/pages/nextSteps").session(new MockHttpSession()))
        .andExpect(status().isOk());
    FormPage formPage = new FormPage(resultActions);
    List<String> nextStepSections = formPage.getElementsByClassName("next-step-section").stream()
        .map(Element::text).collect(Collectors.toList());

    assertThat(nextStepSections).containsExactly(expectedMessages.toArray(new String[0]));
  }
}
