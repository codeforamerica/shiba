package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityParameters;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnapExpeditedEligibilityParserTest {
    private SnapExpeditedEligibilityParser snapExpeditedEligibilityParser;
    private TestApplicationDataBuilder applicationDataBuilder;
    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);
    private final List<JobIncomeInformation> jobIncomeInformation = List.of(mock(JobIncomeInformation.class));
    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    private static final String UTILITY_SELECTION = "some utility";

    @BeforeEach
    void setUp() {
        applicationDataBuilder = new TestApplicationDataBuilder()
                .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of("1"))
                .withPageData("liquidAssets", "liquidAssets", List.of("2"))
                .withPageData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", List.of("false"))
                .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of("3"))
                .withPageData("utilityPayments", "payForUtilities", List.of(UTILITY_SELECTION))
                .withApplicantPrograms(List.of("SNAP"));

        when(grossMonthlyIncomeParser.parse(any())).thenReturn(jobIncomeInformation);
        snapExpeditedEligibilityParser = new SnapExpeditedEligibilityParser(grossMonthlyIncomeParser);
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
                .withHouseholdMemberPrograms(List.of("SNAP"))
                .withPageData("preparingMealsTogether", "isPreparingMealsTogether", List.of("true"))
                .build();

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(
                Money.parse("2"), Money.ONE, jobIncomeInformation,
                "false", Money.parse("3"), List.of(UTILITY_SELECTION), null, false, true, true));
    }

    @Test
    void shouldUseDefaultValueWhenPageDataIsNotAvailable() {
        ApplicationData applicationData = applicationDataBuilder.build();
        applicationData.getPagesData().remove("liquidAssets");

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        Money expectedDefaultValue = Money.parse("0");
        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(expectedDefaultValue, Money.ONE, jobIncomeInformation, "false", Money.parse("3"), List.of(UTILITY_SELECTION), null, true, false, false));
    }

    @Test
    void shouldUseDefaultValueWhenInputValueIsEmpty() {
        ApplicationData applicationData = applicationDataBuilder
                .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of(""))
                .build();

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        Money expectedDefaultValue = Money.parse("0");
        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), expectedDefaultValue, jobIncomeInformation, "false", Money.parse("3"), List.of(UTILITY_SELECTION), null, true, false, false));
    }

    @Test
    void shouldReturnNullParameterWhenMigrantWorkerIsMissing() {
        ApplicationData applicationData = applicationDataBuilder.build();
        applicationData.getPagesData().remove("migrantFarmWorker");

        SnapExpeditedEligibilityParameters parameters = snapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new SnapExpeditedEligibilityParameters(Money.parse("2"), Money.ONE, jobIncomeInformation, null, Money.parse("3"), List.of(UTILITY_SELECTION), null, true, false, false));
    }
}