package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityParameters;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.pages.data.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnapExpeditedEligibilityParserTest extends AbstractParserTest {
    private SnapExpeditedEligibilityParser snapExpeditedEligibilityParser;
    private TestApplicationDataBuilder applicationDataBuilder;
    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);
    private final List<JobIncomeInformation> jobIncomeInformation = List.of(mock(JobIncomeInformation.class));
    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    private static final String UTILITY_SELECTION = "some utility";

    @BeforeEach
    void setUp() {
        applicationDataBuilder = new TestApplicationDataBuilder()
                .withPageData("incomePage", "incomeInput", List.of("1"))
                .withPageData("assetsPage", "assetsInput", List.of("2"))
                .withPageData("migrantWorkerPage", "migrantWorkerInput", List.of("false"))
                .withPageData("housingCostsPage", "housingCostsInput", List.of("3"))
                .withPageData("utilityExpensesSelectionsPage", "utilityExpensesSelectionsInput", List.of(UTILITY_SELECTION))
                .withApplicantPrograms(List.of("SNAP"));

        when(grossMonthlyIncomeParser.parse(any())).thenReturn(jobIncomeInformation);
        snapExpeditedEligibilityParser = new SnapExpeditedEligibilityParser(parsingConfiguration, grossMonthlyIncomeParser);
    }

    @Test
    void shouldParseAllConfiguredExpeditedEligibilityInputs() {
        ApplicationData applicationData = applicationDataBuilder.build();
        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), Money.ONE, jobIncomeInformation, "false", Money.parse("3"), List.of(UTILITY_SELECTION), null, true, false, false));
    }

    @Test
    void shouldParseExpeditedEligibilityInputsWhenHouseholdMemberAppliesForSnap() {
        ApplicationData applicationData = applicationDataBuilder
                .withApplicantPrograms(List.of("GRH"))
                .withPageData("preparingMealsTogether", "isPreparingMealsTogether", List.of("true"))
                .build();

        Subworkflows subworkflows = new Subworkflows();
        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdPrograms", Map.of(
                        "householdPrograms", List.of("SNAP")
                ))
        ));
        subworkflows.addIteration("householdGroup", pagesData);
        applicationData.setSubworkflows(subworkflows);

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(
                Money.parse("2"), Money.ONE, jobIncomeInformation,
                "false", Money.parse("3"), List.of(UTILITY_SELECTION), null, false, true, true));
    }

    @Test
    void shouldUseDefaultValueWhenPageDataIsNotAvailable() {
        ApplicationData applicationData = applicationDataBuilder.build();
        applicationData.getPagesData().remove("assetsPage");

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("100"), Money.ONE, jobIncomeInformation, "false", Money.parse("3"), List.of(UTILITY_SELECTION), null, true, false, false));
    }

    @Test
    void shouldUseDefaultValueWhenInputValueIsEmpty() {
        ApplicationData applicationData = applicationDataBuilder
                .withPageData("incomePage", "incomeInput", List.of(""))
                .build();

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), Money.parse("200"), jobIncomeInformation, "false", Money.parse("3"), List.of(UTILITY_SELECTION), null, true, false, false));
    }

    @Test
    void shouldReturnNullParameterWhenMigrantWorkerIsMissing() {
        ApplicationData applicationData = applicationDataBuilder.build();
        applicationData.getPagesData().remove("migrantWorkerPage");

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), Money.ONE, jobIncomeInformation, null, Money.parse("3"), List.of(UTILITY_SELECTION), null, true, false, false));
    }
}