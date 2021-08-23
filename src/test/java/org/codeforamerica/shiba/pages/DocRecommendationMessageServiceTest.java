package org.codeforamerica.shiba.pages;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.AbstractPageControllerTest;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpSession;

public class DocRecommendationMessageServiceTest extends AbstractPageControllerTest {

  private static final String proofOfIncome = "proofOfIncome";
  private static final String proofOfJobLoss = "proofOfJobLoss";
  private static final String proofOfHousingCost = "proofOfHousingCost";
  private static final String proofOfMedicalExpenses = "proofOfMedicalExpenses";

  @SuppressWarnings("unused")
  private static Stream<Arguments> docRecommendationMessageTestCases() {
    //send over: test name, list of programs, list of doc recs to show, string pagename
    return Stream.of(
        Arguments.of(
            "Show Proof Of Income - Short",
            List.of("CASH", "SNAP"),
            List.of(proofOfIncome),
            "/pages/uploadDocuments",
            "Proof of Income"
        ),
        Arguments.of(
            "Show Proof Of Income - Long",
            List.of("CASH", "SNAP"),
            List.of(proofOfIncome),
            "/pages/documentRecommendation",
            "Proof of Income"
        ),
        Arguments.of(
            "Show Proof of Job Loss - Short",
            List.of("SNAP", "CASH", "GRH"),
            List.of(proofOfJobLoss),
            "/pages/uploadDocuments",
            "Proof of Job Loss"
        ),
        Arguments.of(
            "Show Proof of Job Loss - Long",
            List.of("SNAP", "CASH", "GRH"),
            List.of(proofOfJobLoss),
            "/pages/documentRecommendation",
            "Proof of Job Loss"
        ),
        Arguments.of(
            "Show Proof of Housing Cost - Short",
            List.of("EA"),
            List.of(proofOfHousingCost),
            "/pages/uploadDocuments",
            "Proof of Housing Cost"
        ),
        Arguments.of(
            "Show Proof of Housing Cost - Long",
            List.of("EA"),
            List.of(proofOfHousingCost),
            "/pages/documentRecommendation",
            "Proof of Housing Cost"
        ),
        Arguments.of(
            "Show Proof of Medical Expenses - Short",
            List.of("CCAP"),
            List.of(proofOfMedicalExpenses),
            "/pages/uploadDocuments",
            "Proof of Medical Expenses"
        ),
        Arguments.of(
            "Show Proof of Medical Expenses - Long",
            List.of("CCAP"),
            List.of(proofOfMedicalExpenses),
            "/pages/documentRecommendation",
            "Proof of Medical Expenses"
        )
    );
  }

  @BeforeEach
  void setUp() {
    //what needs to happen before getting to the docRecommendation page
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

  @ParameterizedTest(name = "{0}")
  @MethodSource("org.codeforamerica.shiba.pages.DocRecommendationMessageServiceTest#docRecommendationMessageTestCases")
  @SuppressWarnings("unused")
  void displaysCorrectDocumentRecommendationForApplicantPrograms(String testName,
      List<String> programs, List<String> recommendations, String pageName, String expectedMessage)
      throws Exception {
    setPageInformation(programs, recommendations);

    mockMvc.perform(get(pageName).session(new MockHttpSession()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(expectedMessage)));
  }

  @Test
  void shouldDisplayDocumentRecommendationsForHouseholdMembers() throws Exception {
    var applicantPrograms = List.of("CCAP");
    setPageInformation(applicantPrograms,
        List.of(proofOfJobLoss, proofOfIncome, proofOfHousingCost));

    // Proof of Income is only recommended for snap applications. Add SNAP for household member so it shows
    applicationData.setSubworkflows(new Subworkflows(Map.of("household", new Subworkflow(List.of(
        new PagesDataBuilder().build(List.of(
            new PageDataBuilder("householdMemberInfo", Map.of("programs", List.of("SNAP")))))
    )))));

    mockMvc.perform(get("/pages/documentRecommendation").session(new MockHttpSession()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Proof of Housing Cost")))
        .andExpect(content().string(containsString("Proof of Job Loss")))
        .andExpect(content().string(containsString("Proof of Income")));
  }

  @Test
  void displayCorrectDocumentRecommednationsForJobLossAndIncomeApplicantPrograms()
      throws Exception {
    setPageInformation(List.of("CASH", "EA", "CCAP"),
        List.of(proofOfIncome, proofOfJobLoss, proofOfHousingCost, proofOfMedicalExpenses));

    mockMvc.perform(get("/pages/uploadDocuments").session(new MockHttpSession()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Proof of Income")))
        .andExpect(content().string(containsString("Proof of Job Loss")))
        .andExpect(content().string(containsString("Proof of Housing Cost")))
        .andExpect(content().string(containsString("Proof of Medical Expenses")));
  }

  @Test
  void displayNoDocumentRecommendationsForMinimumFlowSnapApplication() throws Exception {
    // passing no recommendations emulates minimum flow
    setPageInformation(List.of("SNAP"), emptyList());

    var modelAndView = mockMvc.perform(get("/pages/uploadDocuments").session(new MockHttpSession()))
        .andReturn().getModelAndView();
    var recommendations = (ArrayList<?>) Objects.requireNonNull(modelAndView).getModel()
        .get("docRecommendations");
    assertThat(recommendations).isEmpty();
  }

  private void setPageInformation(List<String> programs, List<String> recommendations) {
    var pagesData = new ArrayList<PageDataBuilder>();
    pagesData.add(new PageDataBuilder("choosePrograms", Map.of("programs", programs)));

    recommendations.forEach(recommendation -> {
      PageDataBuilder pageDataBuilder;
      switch (recommendation) {
        case proofOfIncome -> pageDataBuilder = new PageDataBuilder("employmentStatus",
            Map.of("areYouWorking", List.of("true")));
        case proofOfHousingCost -> pageDataBuilder = new PageDataBuilder("homeExpenses",
            Map.of("homeExpenses", List.of("RENT")));
        case proofOfJobLoss -> pageDataBuilder = new PageDataBuilder("workSituation",
            Map.of("hasWorkSituation", List.of("true")));
        case proofOfMedicalExpenses -> pageDataBuilder = new PageDataBuilder("medicalExpenses",
            Map.of("medicalExpenses", List.of("MEDICAL_INSURANCE_PREMIUMS")));
        default -> pageDataBuilder = null;
      }
      if (pageDataBuilder != null) {
        pagesData.add(pageDataBuilder);
      }
    });

    applicationData.setPagesData(new PagesDataBuilder().build(pagesData));
  }
}
