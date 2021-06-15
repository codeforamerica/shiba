package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnapExpeditedEligibilityDeciderTest {
    private TestApplicationDataBuilder applicationDataBuilder;

    UtilityDeductionCalculator mockUtilityDeductionCalculator = mock(UtilityDeductionCalculator.class);
    GrossMonthlyIncomeParser grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);
    TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);

    SnapExpeditedEligibilityDecider decider = new SnapExpeditedEligibilityDecider(
            mockUtilityDeductionCalculator, totalIncomeCalculator, grossMonthlyIncomeParser
    );

    @BeforeEach
    void setup() {
        // Initialize with eligible snap
        applicationDataBuilder = new TestApplicationDataBuilder()
                .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of("1"))
                .withPageData("liquidAssets", "liquidAssets", List.of("2"))
                .withPageData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", List.of("false"))
                .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of("3"))
                .withPageData("utilityPayments", "payForUtilities", List.of("utility"))
                .withApplicantPrograms(List.of("SNAP"));

        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(Money.ZERO);
        when(grossMonthlyIncomeParser.parse(applicationDataBuilder.build())).thenReturn(emptyList());
        when(totalIncomeCalculator.calculate(any())).thenReturn(Money.ONE);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "149,100,ELIGIBLE",
            "151,100,NOT_ELIGIBLE",
            "149,101,NOT_ELIGIBLE",
            "150,101,NOT_ELIGIBLE",
    })
    void shouldQualify_whenMeetingIncomeAndAssetsThresholds(
            String incomeString,
            String assetString,
            SnapExpeditedEligibility expectedDecision
    ) {
        Money income = Money.parse(incomeString);
        when(totalIncomeCalculator.calculate(new TotalIncome(income, emptyList()))).thenReturn(income);

        ApplicationData applicationData = applicationDataBuilder
                .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of(incomeString))
                .withPageData("liquidAssets", "liquidAssets", List.of(assetString))
                .build();

        assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "100,true,ELIGIBLE",
            "101,true,NOT_ELIGIBLE",
            "100,false,NOT_ELIGIBLE",
            "101,false,NOT_ELIGIBLE"
    })
    void shouldQualify_whenApplicantIsMigrantWorkerAndMeetAssetThreshold(
            String assets,
            String isMigrantWorker,
            SnapExpeditedEligibility expectedDecision
    ) {
        when(totalIncomeCalculator.calculate(any())).thenReturn(Money.parse("9999"));

        ApplicationData applicationData = applicationDataBuilder
                .withPageData("liquidAssets", "liquidAssets", List.of(assets))
                .withPageData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", List.of(isMigrantWorker))
                .build();

        assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "500,1000,ELIGIBLE",
            "500,499,NOT_ELIGIBLE",
    })
    void shouldQualifyWhenIncomeAndAssetsAreLessThanExpenses(
            String assets, String housingCosts, SnapExpeditedEligibility expectedDecision
    ) {
        List<String> utilitySelections = List.of("utility");
        when(mockUtilityDeductionCalculator.calculate(utilitySelections)).thenReturn(Money.ONE);

        ApplicationData applicationData = applicationDataBuilder
                .withPageData("liquidAssets", "liquidAssets", List.of(assets))
                .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of(housingCosts))
                .withPageData("utilityPayments", "payForUtilities", utilitySelections)
                .build();

        assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldQualifyWhenHouseholdMemberAppliesForSnapAndPreparesMealsTogether() {
        ApplicationData applicationData = applicationDataBuilder
                .withApplicantPrograms(List.of("CASH"))
                .withHouseholdMemberPrograms(List.of("SNAP"))
                .withPageData("preparingMealsTogether", "isPreparingMealsTogether", List.of("false"))
                .build();
        assertThat(decider.decide(applicationData)).isEqualTo(NOT_ELIGIBLE);

        applicationData = applicationDataBuilder
                .withPageData("preparingMealsTogether", "isPreparingMealsTogether", List.of("true"))
                .build();
        assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
    }

    @Test
    void shouldDefaultToZeroIncomeWhenMissingIncomeAndAssets() {
        ApplicationData applicationData = applicationDataBuilder.build();
        applicationData.getPagesData().remove("thirtyDayIncome");
        applicationData.getPagesData().remove("liquidAssets");

        assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
    }

    @Test
    void shouldNotQualify_whenIncomeAndAssetsAreGreaterThanOrEqualToExpenses_andNotMeetingOtherCriteria() {
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(Money.parse("1000"));
        when(totalIncomeCalculator.calculate(any())).thenReturn(Money.parse("1000"));

        ApplicationData applicationData = applicationDataBuilder
                .withPageData("liquidAssets", "liquidAssets", List.of("1000"))
                .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of("500"))
                .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of("1"))
                .build();

        assertThat(decider.decide(applicationData)).isEqualTo(NOT_ELIGIBLE);
    }

    @Test
    void undeterminedWhenMissingRequiredInformation() {
        ApplicationData applicationData = applicationDataBuilder
                .build();
        applicationData.getPagesData().remove("migrantFarmWorker");

        assertThat(decider.decide(applicationData)).isEqualTo(UNDETERMINED);

        applicationData = applicationDataBuilder
                .withPageData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", List.of("false"))
                .build();
        applicationData.getPagesData().remove("utilityPayments");

        assertThat(decider.decide(applicationData)).isEqualTo(UNDETERMINED);
    }

    @Test
    void undeterminedWhenThereAreBlankThirtyDayEstimates() {
        ApplicationData applicationData = applicationDataBuilder
                .withJobs()
                .build();

        assertThat(decider.decide(applicationData)).isEqualTo(UNDETERMINED);
    }

    @Test
    void shouldNotQualify_whenNoSnap() {
        ApplicationData applicationData = applicationDataBuilder
                .withApplicantPrograms(List.of("CASH"))
                .build();

        assertThat(decider.decide(applicationData)).isEqualTo(NOT_ELIGIBLE);
    }
}