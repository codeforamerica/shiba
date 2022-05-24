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
import org.codeforamerica.shiba.output.caf.ExpeditedCcap;
import org.codeforamerica.shiba.output.caf.ExpeditedSnap;
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
            "Example 1 (Only Expedited SNAP)",
            List.of(SNAP),
            ExpeditedSnap.ELIGIBLE,
            ExpeditedCcap.NOT_ELIGIBLE,
            List.of(
                "Within 24 hours, expect a call from your county about your food assistance application.",
                "If you don't hear from your county within 3 days or want an update on your case, please call your county."
            )
        )
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("org.codeforamerica.shiba.pages.NextStepsContentServiceTest#nextStepMessageTestCases")
  void displaysCorrectSuccessMessageForApplicantPrograms(String testName, List<String> programs,
      ExpeditedSnap expeditedSnap,
      ExpeditedCcap expeditedCcap, List<String> expectedMessages)
      throws Exception {
    PageData programsPage = new PageData();
    programsPage.put("programs", InputData.builder().value(programs).build());
    applicationData.getPagesData().put("choosePrograms", programsPage);

    when(snapExpeditedEligibilityDecider.decide(any())).thenReturn(expeditedSnap);
    when(ccapExpeditedEligibilityDecider.decide(any())).thenReturn(expeditedCcap);

    ResultActions resultActions = mockMvc.perform(
            get("/pages/nextSteps").session(new MockHttpSession()))
        .andExpect(status().isOk());
    FormPage formPage = new FormPage(resultActions);
    List<String> nextStepSections = formPage.getElementsByClassName("next-step-section").stream()
        .map(Element::text).collect(Collectors.toList());

    assertThat(nextStepSections).containsExactly(expectedMessages.toArray(new String[0]));
  }
}
