package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class UnearnedIncomeCalculatorTest {

  UnearnedIncomeCalculator unearnedIncomeCalculator = new UnearnedIncomeCalculator();
  private final TestApplicationDataBuilder applicationDataBuilder = new TestApplicationDataBuilder();

  @Test
  void unearnedIncomeCafShouldCalculateTo360() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("unearnedIncomeSources", "socialSecurityAmount", "140")
        .withPageData("unearnedIncomeSources", "supplementalSecurityIncomeAmount",
            List.of("49.50", "49.50"))
        .withPageData("unearnedIncomeSources", "veteransBenefitsAmount", "10")
        .withPageData("unearnedIncomeSources", "unemploymentAmount", List.of())
        .withPageData("unearnedIncomeSources", "workersCompensationAmount", "30")
        .withPageData("unearnedIncomeSources", "retirementAmount", List.of("40", "0"))
        .withPageData("unearnedIncomeSources", "childOrSpousalSupportAmount", List.of())
        .withPageData("unearnedIncomeSources", "tribalPaymentsAmount", List.of("41", ""))
        .build();
    Money totalUnearnedIncome = unearnedIncomeCalculator.unearnedAmount(applicationData);
    assertThat(totalUnearnedIncome).isEqualTo(Money.parse("360.00"));
  }

  @Test
  void unearnedIncomeCafShouldCalculateToZeroWhenFieldsAreBlank() {
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
    Money totalUnearnedIncome = unearnedIncomeCalculator.unearnedAmount(applicationData);

    assertThat(totalUnearnedIncome).isEqualTo(Money.parse("0.00"));
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
        Money totalUnearnedIncome = unearnedIncomeCalculator.unearnedAmount(applicationData);

        assertThat(totalUnearnedIncome).isEqualTo(Money.parse("1360.00"));
  }

  @Test
  void otherUnearnedIncomeShouldCalculateTo420() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", "139")
        .withPageData("insurancePaymentsIncomeSource", "insurancePaymentsAmount", "100")
        .withPageData("contractForDeedIncomeSource", "contractForDeedAmount", "10")
        .withPageData("trustMoneyIncomeSource", "trustMoneyAmount", "80")
        .withPageData("healthcareReimbursementIncomeSource",
            "healthCareReimbursementAmount", "30")
        .withPageData("interestDividendsIncomeSource", "interestDividendsAmount", "40")
        .withPageData("rentalIncomeSource", "rentalIncomeAmount", "1")
        .withPageData("otherPaymentsIncomeSource", "otherPaymentsAmount", "20")
        .build();
    Money totalUnearnedIncome = unearnedIncomeCalculator.unearnedAmount(applicationData);
    assertThat(totalUnearnedIncome).isEqualTo(Money.parse("420"));
  }

  @Test
  void otherUnearnedIncomeShouldCalculateToZeroWhenFieldsAreBlank() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", List.of())
        .withPageData("insurancePaymentsIncomeSource", "insurancePaymentsAmount", List.of())
        .withPageData("contractForDeedIncomeSource", "contractForDeedAmount", List.of())
        .withPageData("trustMoneyIncomeSource", "trustMoneyAmount", List.of())
        .withPageData("healthcareReimbursementIncomeSource",
            "healthCareReimbursementAmount", List.of())
        .withPageData("interestDividendsIncomeSource", "interestDividendsAmount", List.of())
        .withPageData("rentalIncomeSource", "rentalIncomeAmount", List.of())
        .withPageData("otherPaymentsIncomeSource", "otherPaymentsAmount", List.of())
        .build();
    Money totalUnearnedIncome = unearnedIncomeCalculator.unearnedAmount(applicationData);

    assertThat(totalUnearnedIncome).isEqualTo(Money.parse("0"));
  }

  @Test
  void otherUnearnedIncomeShouldIgnoreNonNumberCharacters() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("benefitsProgramsIncomeSource", "benefitsAmount", "138.10")
        .withPageData("insurancePaymentsIncomeSource", "insurancePaymentsAmount", "100.90")
        .withPageData("contractForDeedIncomeSource", "contractForDeedAmount", "1,010")
        .withPageData("trustMoneyIncomeSource", "trustMoneyAmount", List.of())
        .withPageData("healthcareReimbursementIncomeSource", "healthCareReimbursementAmount",
            "30")
        .withPageData("interestDividendsIncomeSource", "interestDividendsAmount", "40")
        .withPageData("rentalIncomeSource", "rentalIncomeAmount", List.of())
        .withPageData("otherPaymentsIncomeSource", "otherPaymentsAmount", List.of())
        .build();
    Money totalUnearnedIncome = unearnedIncomeCalculator.unearnedAmount(applicationData);

    assertThat(totalUnearnedIncome).isEqualTo(Money.parse("1319.00"));
  }
}
