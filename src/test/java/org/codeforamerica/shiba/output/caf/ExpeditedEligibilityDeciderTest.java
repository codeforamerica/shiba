package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.InputData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpeditedEligibilityDeciderTest {
    HashMap<String, InputData> inputs = new HashMap<>();

    UtilityDeductionCalculator mockUtilityDeductionCalculator = mock(UtilityDeductionCalculator.class);
    ExpeditedEligibilityDecider decider = new ExpeditedEligibilityDecider(mockUtilityDeductionCalculator);

    @BeforeEach
    void setup() {
        inputs.put("expeditedIncome_moneyMadeLast30Days", new InputData(List.of("99999")));
        inputs.put("liquidAssets_liquidAssets", new InputData(List.of("99999")));
        inputs.put("migrantFarmWorker_migrantOrSeasonalFarmWorker", new InputData(List.of("false")));
        inputs.put("expeditedExpensesAmount_expeditedExpensesAmount", new InputData(List.of("99")));
        inputs.put("expeditedUtilityPayments_payForUtilities", new InputData(List.of()));
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(0);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "149,100,true",
            "151,100,false",
            "149,101,false",
            "150,101,false",
    })
    void shouldQualify_whenMeetingIncomeAndAssetsThresholds(
            String income,
            String assets,
            boolean expectedDecision
    ) {
        inputs.put("expeditedIncome_moneyMadeLast30Days", new InputData(List.of(income)));
        inputs.put("liquidAssets_liquidAssets", new InputData(List.of(assets)));

        assertThat(decider.decide(inputs)).isEqualTo(expectedDecision);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "100,true,true",
            "101,true,false",
            "100,false,false",
            "101,false,false"
    })
    void shouldQualify_whenApplicantIsMigrantWorkerAndMeetAssetThreshold(
            String assets,
            String isMigrantWorker,
            boolean expectedDecision
    ) {
        inputs.put("liquidAssets_liquidAssets", new InputData(List.of(assets)));
        inputs.put("migrantFarmWorker_migrantOrSeasonalFarmWorker", new InputData(List.of(isMigrantWorker)));

        assertThat(decider.decide(inputs)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldQualify_whenIncomeAndAssetsAreLessThanExpenses() {
        String income = "500";
        String assets = "1000";
        String rentMortgageAmount = "500";
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(1001);

        inputs.put("expeditedIncome_moneyMadeLast30Days", new InputData(List.of(income)));
        inputs.put("liquidAssets_liquidAssets", new InputData(List.of(assets)));
        inputs.put("expeditedExpensesAmount_expeditedExpensesAmount", new InputData(List.of(rentMortgageAmount)));

        assertThat(decider.decide(inputs)).isEqualTo(true);
    }

    @Test
    void shouldNotQualify_whenIncomeAndAssetsAreGreaterThanOrEqualToExpenses_andNotMeetingOtherCriteria() {
        String income = "500";
        String assets = "1000";
        String rentMortgageAmount = "500";
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(1000);

        inputs.put("expeditedIncome_moneyMadeLast30Days", new InputData(List.of(income)));
        inputs.put("liquidAssets_liquidAssets", new InputData(List.of(assets)));
        inputs.put("expeditedExpensesAmount_expeditedExpensesAmount", new InputData(List.of(rentMortgageAmount)));

        assertThat(decider.decide(inputs)).isEqualTo(false);
    }

    @Test
    void shouldNotQualify_whenNeededDataIsNotPresent() {
        assertThat(decider.decide(new HashMap<>())).isEqualTo(false);
    }
}