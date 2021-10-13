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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.testutilities.AbstractPageControllerTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

public class NextStepsContentServiceTest extends AbstractPageControllerTest {
  private static final String CCAP = "CCAP";
  private static final String SNAP = "SNAP";
  private static final String GRH = "GRH";
  private static final String CASH = "CASH";
  private static final String EA = "EA";

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

  private static Stream<Arguments> nextStepMessageTestCases() {
    return Stream.of(
        Arguments.of(
            "Example 1 (Only Expedited SNAP)", // test Name
            List.of(SNAP), // Programs they're applying for
            SnapExpeditedEligibility.ELIGIBLE, // Eligible for SNAP expedited service
            CcapExpeditedEligibility.NOT_ELIGIBLE, // Eligible for CCAP expedited service
            List.of( // expected messages at the end.
                "Within 24 hours, expect a call from your county about your food assistance application.",
                "If you don't hear from your county within 3 days or want an update on your case, please call your county."
            )
        ),
        Arguments.of(
            "Example 2", // test Name
            List.of(SNAP, CCAP), // Programs they're applying for
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of( // expected messages at the end.
                "Within 24 hours, expect a call from your county about your food assistance application.",
                "In the next 7-10 days, expect to get a letter in the mail from your county about your childcare application. The letter will explain your next steps.",
                "If you don't hear from your county within 3 days or want an update on your case, please call your county."
            )
        ),
        Arguments.of(
            "Example 3",
            List.of(SNAP, CCAP, CASH, EA),
            SnapExpeditedEligibility.NOT_ELIGIBLE,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of( // expected messages at the end.
                "Within 5 days, your county will determine your childcare assistance case and send you a letter in the mail.",
                "In the next 7-10 days, expect to get a letter in the mail from your county about your emergency assistance, cash support and food support application. The letter will explain your next steps.",
                "Call your county if you don’t hear from them in the time period we’ve noted."
            )
        )
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("org.codeforamerica.shiba.pages.NextStepsContentServiceTest#nextStepMessageTestCases")
  void displaysCorrectSuccessMessageForApplicantPrograms(
      String testName,
      List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      List<String> expectedMessages)
      throws Exception {
    PageData programsPage = new PageData();
    programsPage.put("programs", InputData.builder().value(programs).build());
    applicationData.getPagesData().put("choosePrograms", programsPage);

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
