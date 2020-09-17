package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.output.TotalIncome;
import org.codeforamerica.shiba.output.TotalIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.caf.ExpeditedEligibility.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpeditedEligibilityDeciderTest {
    private final PagesData pagesData = new PagesData();
    private final ApplicationData applicationData = new ApplicationData();

    UtilityDeductionCalculator mockUtilityDeductionCalculator = mock(UtilityDeductionCalculator.class);
    ExpeditedEligibilityParser expeditedEligibilityParser = mock(ExpeditedEligibilityParser.class);
    TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);

    ExpeditedEligibilityDecider decider = new ExpeditedEligibilityDecider(
            mockUtilityDeductionCalculator, totalIncomeCalculator, expeditedEligibilityParser
    );

    @BeforeEach
    void setup() {
        applicationData.setPagesData(pagesData);
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(0);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "149,100,ELIGIBLE",
            "151,100,NOT_ELIGIBLE",
            "149,101,NOT_ELIGIBLE",
            "150,101,NOT_ELIGIBLE",
    })
    void shouldQualify_whenMeetingIncomeAndAssetsThresholds(
            Double income,
            Double assets,
            ExpeditedEligibility expectedDecision
    ) {
        when(totalIncomeCalculator.calculate(new TotalIncome(income, emptyList()))).thenReturn(income);
        when(expeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(new ExpeditedEligibilityParameters(assets, income, emptyList(), false, 0.0, emptyList())));

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
            Double assets,
            Boolean isMigrantWorker,
            ExpeditedEligibility expectedDecision
    ) {
        when(totalIncomeCalculator.calculate(any())).thenReturn(9999.0);
        when(expeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(
                new ExpeditedEligibilityParameters(assets, 9999.0, emptyList(), isMigrantWorker, 0.0, emptyList())));

        assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldQualify_whenIncomeAndAssetsAreLessThanExpenses() {
        List<String> utilitySelections = List.of("utility");
        when(mockUtilityDeductionCalculator.calculate(utilitySelections)).thenReturn(1001);
        when(totalIncomeCalculator.calculate(any())).thenReturn(500.0);
        when(expeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(
                new ExpeditedEligibilityParameters(1000, 500, emptyList(),false, 500.0, utilitySelections)));

        assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
    }

    @Test
    void shouldNotQualify_whenIncomeAndAssetsAreGreaterThanOrEqualToExpenses_andNotMeetingOtherCriteria() {
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(1000);
        when(totalIncomeCalculator.calculate(any())).thenReturn(500.0);
        when(expeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(
                new ExpeditedEligibilityParameters(1000, 500, emptyList(),false, 500.0, emptyList())));

        assertThat(decider.decide(applicationData)).isEqualTo(NOT_ELIGIBLE);
    }

    @Test
    void shouldNotQualify_whenNeededDataIsNotPresent() {
        when(expeditedEligibilityParser.parse(any())).thenReturn(Optional.empty());
        assertThat(decider.decide(applicationData)).isEqualTo(UNDETERMINED);
    }
}