package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.util.List;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;

class UnearnedIncomeCcapCalculatorTest {
  UnearnedIncomeCcapCalculator unearnedIncomeCcapCalculator = new UnearnedIncomeCcapCalculator();
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
        .withPageData("unearnedIncome", "unearnedIncome", List.of("SOCIAL_SECURITY","SSI","VETERANS_BENEFITS",
            "UNEMPLOYMENT","WORKERS_COMPENSATION","RETIREMENT","CHILD_OR_SPOUSAL_SUPPORT","TRIBAL_PAYMENTS"))
        .withPageData("unearnedIncomeCcap", "unearnedIncomeCcap", List.of("BENEFITS","INSURANCE_PAYMENTS","CONTRACT_FOR_DEED",
            "TRUST_MONEY","HEALTH_CARE_REIMBURSEMENT","INTEREST_DIVIDENDS","OTHER_SOURCES"))
        
        .withApplicantPrograms(List.of("SNAP"));
  }

  @Test
  @MethodSource
  void unearnedIncomeCcapShouldCalculateto319() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("unearnedIncomeSourcesCcap", "benefitsAmount","139" )
        .withPageData("unearnedIncomeSourcesCcap", "insurancePaymentsAmount","100" )
        .withPageData("unearnedIncomeSourcesCcap", "contractForDeedAmount","10" )
        .withPageData("unearnedIncomeSourcesCcap", "trustMoneyAmount",List.of() )
        .withPageData("unearnedIncomeSourcesCcap", "healthCareReimbursementAmount","30" )
        .withPageData("unearnedIncomeSourcesCcap", "interestDividendsAmount","40" )
        .withPageData("unearnedIncomeSourcesCcap", "otherSourcesAmount",List.of() )
        .build();
    BigDecimal totalUnearnedIncome = unearnedIncomeCcapCalculator.unearnedAmount(applicationData);
    assertThat(totalUnearnedIncome).isEqualTo(new BigDecimal("319"));
  }
  
  @Test
  @MethodSource
  void unearnedIncomeCcapShouldCalculatetoZeroWhenFieldsAreBlank() {
    ApplicationData applicationData = applicationDataBuilder
        .withPageData("unearnedIncomeSourcesCcap", "benefitsAmount",List.of() )
        .withPageData("unearnedIncomeSourcesCcap", "insurancePaymentsAmount",List.of() )
        .withPageData("unearnedIncomeSourcesCcap", "contractForDeedAmount",List.of() )
        .withPageData("unearnedIncomeSourcesCcap", "trustMoneyAmount",List.of() )
        .withPageData("unearnedIncomeSourcesCcap", "healthCareReimbursementAmount",List.of() )
        .withPageData("unearnedIncomeSourcesCcap", "interestDividendsAmount",List.of() )
        .withPageData("unearnedIncomeSourcesCcap", "otherSourcesAmount",List.of() )
        .build();
    BigDecimal totalUnearnedIncome = unearnedIncomeCcapCalculator.unearnedAmount(applicationData);

    assertThat(totalUnearnedIncome).isEqualTo(new BigDecimal("0"));
  }
}
