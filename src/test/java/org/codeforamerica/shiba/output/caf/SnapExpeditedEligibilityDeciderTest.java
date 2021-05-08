package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.parsers.SnapExpeditedEligibilityParser;
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
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnapExpeditedEligibilityDeciderTest {
    private final PagesData pagesData = new PagesData();
    private final ApplicationData applicationData = new ApplicationData();

    UtilityDeductionCalculator mockUtilityDeductionCalculator = mock(UtilityDeductionCalculator.class);
    SnapExpeditedEligibilityParser snapExpeditedEligibilityParser = mock(SnapExpeditedEligibilityParser.class);
    TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);

    SnapExpeditedEligibilityDecider decider = new SnapExpeditedEligibilityDecider(
            mockUtilityDeductionCalculator, totalIncomeCalculator, snapExpeditedEligibilityParser
    );

    @BeforeEach
    void setup() {
        applicationData.setPagesData(pagesData);
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(new Money(0));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "149,100,ELIGIBLE",
            "151,100,NOT_ELIGIBLE",
            "149,101,NOT_ELIGIBLE",
            "150,101,NOT_ELIGIBLE",
    })
    void shouldQualify_whenMeetingIncomeAndAssetsThresholds(
            double incomeString,
            double assetString,
            SnapExpeditedEligibility expectedDecision
    ) {
        Money income = new Money(incomeString);
        Money assets = new Money(assetString);
        when(totalIncomeCalculator.calculate(new TotalIncome(income, emptyList()))).thenReturn(income);
        when(snapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(new SnapExpeditedEligibilityParameters(assets, income, emptyList(), false, new Money(0), emptyList(), true)));

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
            double assets,
            Boolean isMigrantWorker,
            SnapExpeditedEligibility expectedDecision
    ) {
        when(totalIncomeCalculator.calculate(any())).thenReturn(new Money(9999));
        when(snapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(
                new SnapExpeditedEligibilityParameters(new Money(assets), new Money(9999), emptyList(), isMigrantWorker, new Money(0), emptyList(), true)));

        assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
    }

    @Test
    void shouldQualify_whenIncomeAndAssetsAreLessThanExpenses() {
        List<String> utilitySelections = List.of("utility");
        when(mockUtilityDeductionCalculator.calculate(utilitySelections)).thenReturn(new Money(1001));
        when(totalIncomeCalculator.calculate(any())).thenReturn(new Money(500));
        when(snapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(
                new SnapExpeditedEligibilityParameters(new Money(1000), new Money(500), emptyList(),false, new Money(500), utilitySelections, true)));

        assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
    }

    @Test
    void shouldNotQualify_whenIncomeAndAssetsAreGreaterThanOrEqualToExpenses_andNotMeetingOtherCriteria() {
        when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(new Money(1000));
        when(totalIncomeCalculator.calculate(any())).thenReturn(new Money(500));
        when(snapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(
                new SnapExpeditedEligibilityParameters(new Money(1000), new Money(500), emptyList(),false, new Money(500), emptyList(), true)));

        assertThat(decider.decide(applicationData)).isEqualTo(NOT_ELIGIBLE);
    }

    @Test
    void shouldNotQualify_whenNeededDataIsNotPresent() {
        when(snapExpeditedEligibilityParser.parse(any())).thenReturn(Optional.empty());
        assertThat(decider.decide(applicationData)).isEqualTo(UNDETERMINED);
    }

    @Test
    void shouldNotQualify_whenNoSnap() {
        List<String> utilitySelections = List.of("utility");
        when(totalIncomeCalculator.calculate(any())).thenReturn(new Money(0));
        when(snapExpeditedEligibilityParser.parse(applicationData)).thenReturn(Optional.of(
                new SnapExpeditedEligibilityParameters(new Money(1), new Money(1), emptyList(),false, new Money(500), utilitySelections, false)));
        assertThat(decider.decide(applicationData)).isEqualTo(NOT_ELIGIBLE);
    }
}