package org.codeforamerica.shiba.output.caf;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.NOT_ELIGIBLE;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.UNDETERMINED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SnapExpeditedEligibilityDeciderTest {

  UtilityDeductionCalculator mockUtilityDeductionCalculator = mock(
      UtilityDeductionCalculator.class);
  GrossMonthlyIncomeParser grossMonthlyIncomeParser = mock(GrossMonthlyIncomeParser.class);
  TotalIncomeCalculator totalIncomeCalculator = mock(TotalIncomeCalculator.class);
  UnearnedIncomeCalculator unearnedIncomeCalculator = mock(UnearnedIncomeCalculator.class);

  SnapExpeditedEligibilityDecider decider = new SnapExpeditedEligibilityDecider(
      mockUtilityDeductionCalculator, totalIncomeCalculator, grossMonthlyIncomeParser,
      unearnedIncomeCalculator
  );
  private TestApplicationDataBuilder applicationDataBuilder;

  @BeforeEach
  void setup() {
    // Initialize with eligible snap
    applicationDataBuilder = new TestApplicationDataBuilder()
        .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of("1"))
        .withPageData("liquidAssetsSingle", "liquidAssets", List.of("2"))
        .withPageData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", List.of("false"))
        .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of("3"))
        .withPageData("utilityPayments", "payForUtilities", List.of("utility"))
        .withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of())
        .withPageData("otherUnearnedIncome", "unearnedIncome", List.of("BENEFITS"))
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", List.of())
        .withApplicantPrograms(List.of("SNAP"));

    when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(Money.ZERO);
    when(grossMonthlyIncomeParser.parse(applicationDataBuilder.build())).thenReturn(emptyList());
    when(totalIncomeCalculator.calculate(any())).thenReturn(Money.ONE);
    when(unearnedIncomeCalculator.unearnedAmount(any())).thenReturn(Money.ZERO);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "149,100,0,0,ELIGIBLE",
      "139,100,10,0,ELIGIBLE",
      "139,100,0,10,ELIGIBLE",
      "129,100,10,10,ELIGIBLE",
      "51,100,50,50,NOT_ELIGIBLE",
      "49,101,50,50,NOT_ELIGIBLE",
      "50,101,50,50,NOT_ELIGIBLE",
  })
  void shouldQualify_whenMeetingIncomeAndAssetsThresholds(
      String incomeString,
      String assetString,
      String unearnedCafString,
      String unearnedCcapString,
      SnapExpeditedEligibility expectedDecision
  ) {
    Money income = Money.parse(incomeString);
    when(totalIncomeCalculator.calculate(new TotalIncome(income, emptyList()))).thenReturn(income);

    ApplicationData applicationData = applicationDataBuilder
        .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of(incomeString))
        .withPageData("liquidAssetsSingle", "liquidAssets", List.of(assetString))
        .withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of(unearnedCafString))
        .withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("BENEFITS"))
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", List.of(unearnedCcapString))
        .build();
    Money unearnedCaf = Money.parse(unearnedCafString);
    Money unearnedCcap = Money.parse(unearnedCcapString);
    when(unearnedIncomeCalculator.unearnedAmount(applicationData))
        .thenReturn(unearnedCaf.add(unearnedCcap));
    assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "100,true,false,ELIGIBLE",
      "100,true,true,NOT_ELIGIBLE",
      "101,true,false,NOT_ELIGIBLE",
      "100,false,false,NOT_ELIGIBLE",
      "101,false,true,NOT_ELIGIBLE"
  })
  void shouldQualify_whenApplicantIsMigrantWorkerHasNoJobAndMeetAssetThreshold(
      String assets,
      String isMigrantWorker,
      String hasJob,
      SnapExpeditedEligibility expectedDecision
  ) {
    when(totalIncomeCalculator.calculate(any())).thenReturn(Money.parse("9999"));

    ApplicationData applicationData = applicationDataBuilder
        .withPageData("liquidAssetsSingle", "liquidAssets", List.of(assets))
        .withPageData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", List.of(isMigrantWorker))
        .withPageData("employmentStatus", "areYouWorking", hasJob)
        .withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of("999"))
        .withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("BENEFITS"))
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", List.of("999"))
        .build();
    when(unearnedIncomeCalculator.unearnedAmount(applicationData)).thenReturn(
        Money.parse("999"));
    Money.parse("999");
    assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "499,500,0,0,ELIGIBLE",
      "249,500,250,0,ELIGIBLE",
      "249,500,0,250,ELIGIBLE",
      "500,1000,250,249,ELIGIBLE",
      "249,500,251,0,NOT_ELIGIBLE",
      "249,500,0,251,NOT_ELIGIBLE",
      "500,999,500,500,NOT_ELIGIBLE",
  })
  void shouldQualifyWhenIncomeAndAssetsAreLessThanExpenses(
      String assets, String housingCosts, String unearnedIncomeCaf, String otherUnearnedIncome,
      SnapExpeditedEligibility expectedDecision
  ) {
    List<String> utilitySelections = List.of("utility");
    when(mockUtilityDeductionCalculator.calculate(utilitySelections)).thenReturn(Money.ONE);

    ApplicationData applicationData = applicationDataBuilder
        .withPageData("liquidAssetsSingle", "liquidAssets", List.of(assets))
        .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of(housingCosts))
        .withPageData("utilityPayments", "payForUtilities", utilitySelections)
        .withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of(unearnedIncomeCaf))
        .withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("BENEFITS"))
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", List.of(otherUnearnedIncome))
        .build();
    Money unearnedCaf = Money.parse(unearnedIncomeCaf);
    Money unearnedCcap = Money.parse(otherUnearnedIncome);
    when(unearnedIncomeCalculator.unearnedAmount(applicationData))
        .thenReturn(unearnedCaf.add(unearnedCcap));
    assertThat(decider.decide(applicationData)).isEqualTo(expectedDecision);
  }

  @Test
  void shouldQualifyWhenHouseholdMemberAppliesForSnapRegardlessOfPreparesMealsTogether() {
    ApplicationData applicationData = applicationDataBuilder
        .withApplicantPrograms(List.of("CASH"))
        .withHouseholdMemberPrograms(List.of("SNAP"))
        .withPageData("preparingMealsTogether", "isPreparingMealsTogether", List.of("false"))
        .build();
    assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);

    applicationData = applicationDataBuilder
        .withPageData("preparingMealsTogether", "isPreparingMealsTogether", List.of("true"))
        .build();
    assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
  }

  @Test
  void shouldQualifyWhenJobsEarnLessThanThreshold() {
    ApplicationData applicationData = applicationDataBuilder
        .withJobs()
        .build();

    assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
  }

  @Test
  void shouldQualifyWhenNoJobs() {
    ApplicationData applicationData = applicationDataBuilder
        .build();

    assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
  }

  @Test
  void shouldDefaultToZeroIncomeWhenMissingIncome_UnearnedIncomeAndAssets() {
    ApplicationData applicationData = applicationDataBuilder.build();
    applicationData.getPagesData().remove("thirtyDayIncome");
    applicationData.getPagesData().remove("liquidAssetsSingle");
    applicationData.getPagesData().remove("unearnedIncome");
    applicationData.getPagesData().remove("otherUnearnedIncome");

    assertThat(decider.decide(applicationData)).isEqualTo(ELIGIBLE);
  }

  @Test
  void shouldNotQualify_whenIncome_UnearnedIncomeAndAssetsAreGreaterThanOrEqualToExpenses_andNotMeetingOtherCriteria() {
    when(mockUtilityDeductionCalculator.calculate(any())).thenReturn(Money.parse("1000"));
    when(totalIncomeCalculator.calculate(any())).thenReturn(Money.parse("1000"));
    when(unearnedIncomeCalculator.unearnedAmount(any())).thenReturn(Money.parse("500"));

    ApplicationData applicationData = applicationDataBuilder
        .withPageData("liquidAssetsSingle", "liquidAssets", List.of("1000"))
        .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of("500"))
        .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of("1"))
        .withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY"))
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of("500"))
        .withPageData("otherUnearnedIncome", "otherUnearnedIncome", List.of("BENEFITS"))
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", List.of("500"))
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
  void shouldNotQualify_whenNoSnap() {
    ApplicationData applicationData = applicationDataBuilder
        .withApplicantPrograms(List.of("CASH"))
        .build();

    assertThat(decider.decide(applicationData)).isEqualTo(NOT_ELIGIBLE);
  }
}
