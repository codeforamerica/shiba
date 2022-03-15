package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;

class UnearnedIncomeCafCalculatorTest {

  UnearnedIncomeCafCalculator unearnedIncomeCafCalculator = new UnearnedIncomeCafCalculator();
  private TestApplicationDataBuilder applicationDataBuilder;

  @BeforeEach
  void setup() {
    // Initialize with eligible snap
    applicationDataBuilder = new TestApplicationDataBuilder()
        .withPageData("thirtyDayIncome", "moneyMadeLast30Days", List.of("1"))
        .withPageData("liquidAssets", "liquidAssets", List.of("2"))
        .withPageData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", List.of("false"))
        .withPageData("homeExpensesAmount", "homeExpensesAmount", List.of("3"))
        .withPageData("utilityPayments", "payForUtilities", List.of("utility"))
        .withPageData("unearnedIncome", "unearnedIncome",
            List.of("SOCIAL_SECURITY", "SSI", "VETERANS_BENEFITS",
                "UNEMPLOYMENT", "WORKERS_COMPENSATION", "RETIREMENT", "CHILD_OR_SPOUSAL_SUPPORT",
                "TRIBAL_PAYMENTS"))

        .withApplicantPrograms(List.of("SNAP"));
  }

  @Test
  @MethodSource
  void unearnedIncomeCafShouldCalculateto360() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", "139")
        .withPageData("unearnedIncomeSources", "supplementalSecurityIncomeAmount", "100")
        .withPageData("unearnedIncomeSources", "veteransBenefitsAmount", "10")
        .withPageData("unearnedIncomeSources", "unemploymentAmount", List.of())
        .withPageData("unearnedIncomeSources", "workersCompensationAmount", "30")
        .withPageData("unearnedIncomeSources", "retirementAmount", "40")
        .withPageData("unearnedIncomeSources", "childOrSpousalSupportAmount", List.of())
        .withPageData("unearnedIncomeSources", "tribalPaymentsAmount", "41")
        .build();
    BigDecimal totalUnearnedIncome = unearnedIncomeCafCalculator.unearnedAmount(applicationData);
    assertThat(totalUnearnedIncome).isEqualTo(new BigDecimal("360"));
  }

  @Test
  @MethodSource
  void unearnedIncomeCafShouldCalculatetoZeroWhenFieldsAreBlank() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", List.of())
        .withPageData("unearnedIncomeSources", "supplementalSecurityIncomeAmount", List.of())
        .withPageData("unearnedIncomeSources", "veteransBenefitsAmount", List.of())
        .withPageData("unearnedIncomeSources", "unemploymentAmount", List.of())
        .withPageData("unearnedIncomeSources", "workersCompensationAmount", List.of())
        .withPageData("unearnedIncomeSources", "retirementAmount", List.of())
        .withPageData("unearnedIncomeSources", "childOrSpousalSupportAmount", List.of())
        .withPageData("unearnedIncomeSources", "tribalPaymentsAmount", List.of())
        .build();
    BigDecimal totalUnearnedIncome = unearnedIncomeCafCalculator.unearnedAmount(applicationData);

    assertThat(totalUnearnedIncome).isEqualTo(new BigDecimal("0"));
  }

  @Test
  void unearnedIncomeCafShouldIgnoreNonNumberCharacters() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", "138.10")
        .withPageData("unearnedIncomeSources", "supplementalSecurityIncomeAmount", "100.90")
        .withPageData("unearnedIncomeSources", "veteransBenefitsAmount", "1,010")
        .withPageData("unearnedIncomeSources", "unemploymentAmount", List.of())
        .withPageData("unearnedIncomeSources", "workersCompensationAmount", "30")
        .withPageData("unearnedIncomeSources", "retirementAmount", "40")
        .withPageData("unearnedIncomeSources", "childOrSpousalSupportAmount", List.of())
        .withPageData("unearnedIncomeSources", "tribalPaymentsAmount", "41")
        .build();
    BigDecimal totalUnearnedIncome = unearnedIncomeCafCalculator.unearnedAmount(applicationData);

    assertThat(totalUnearnedIncome).isEqualTo(new BigDecimal("1360.00"));
  }
}
