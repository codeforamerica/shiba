package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DocRecommendationMessageServiceTest extends AbstractPageControllerTest {

    private static final String proofOfIncome ="proofOfIncome";
    private static final String proofOfJobLoss ="proofOfJobLoss";
    private static final String proofOfHousingCost ="proofOfHousingCost";
    private static final String proofOfMedicalExpenses ="proofOfMedicalExpenses";

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

    private static Stream<Arguments> docRecommendationMessageTestCases(){
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
                        "Show Proof of Housing Cost - Long",
                        List.of("CCAP"),
                        List.of(proofOfMedicalExpenses),
                        "/pages/documentRecommendation",
                        "Proof of Medical Expenses"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("org.codeforamerica.shiba.pages.DocRecommendationMessageServiceTest#docRecommendationMessageTestCases")
    void displaysCorrectSuccessMessageForApplicantPrograms(String testName, List<String> programs, List<String> recommendations, String pageName, String expectedMessage) throws Exception {
        setPageInformation(programs,recommendations);



        mockMvc.perform(get(pageName).session(new MockHttpSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedMessage)));

    }

    private void setPageInformation(List<String> programs, List<String> recommendations){
        PageDataBuilder programPageData = new PageDataBuilder("choosePrograms", Map.of("programs", programs));

        List<PageDataBuilder> pagesData = new ArrayList<>();
        recommendations.stream().forEach(recommendation -> {
            PageDataBuilder pageDataBuilder;
            switch(recommendation) {
                case proofOfIncome:
                    pageDataBuilder = new PageDataBuilder("employmentStatus", Map.of("areYouWorking", List.of("true")));
                    pagesData.add(pageDataBuilder);
                    break;
                case proofOfHousingCost:
                    pageDataBuilder = new PageDataBuilder("homeExpenses", Map.of("homeExpenses",List.of("RENT")));
                    pagesData.add(pageDataBuilder);
                    break;
                case proofOfJobLoss:
                    pageDataBuilder = new PageDataBuilder("workSituation", Map.of("hasWorkSituation", List.of("true")));
                    pagesData.add(pageDataBuilder);
                    break;
                case proofOfMedicalExpenses:
                    pageDataBuilder = new PageDataBuilder("medicalExpenses", Map.of("medicalExpenses", List.of("MEDICAL_INSURANCE_PREMIUMS")));
                    pagesData.add(pageDataBuilder);
            }
        });

        pagesData.add(programPageData);
        applicationData.setPagesData(new PagesDataBuilder().build(
                pagesData
        ));
    }


}