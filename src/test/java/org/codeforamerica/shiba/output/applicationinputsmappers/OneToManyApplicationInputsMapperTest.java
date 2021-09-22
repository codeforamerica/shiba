package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class OneToManyApplicationInputsMapperTest {

  private final OneToManyApplicationInputsMapper mapper = new OneToManyApplicationInputsMapper();

  @Test
  public void shouldMapNoneOfTheAboveToNoForAllOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
        .withPageData("unearnedIncomeCcap", "unearnedIncomeCcap",
            List.of("NO_UNEARNED_INCOME_CCAP_SELECTED"))
        .withPageData("homeExpenses", "homeExpenses", List.of("NONE_OF_THE_ABOVE"))
        .withPageData("utilityPayments", "payUtilities", List.of("NONE_OF_THE_ABOVE"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("unearnedIncome", "SOCIAL_SECURITY", "false"),
        createApplicationInput("unearnedIncome", "SSI", "false"),
        createApplicationInput("unearnedIncome", "VETERANS_BENEFITS", "false"),
        createApplicationInput("unearnedIncome", "UNEMPLOYMENT", "false"),
        createApplicationInput("unearnedIncome", "WORKERS_COMPENSATION", "false"),
        createApplicationInput("unearnedIncome", "RETIREMENT", "false"),
        createApplicationInput("unearnedIncome", "CHILD_OR_SPOUSAL_SUPPORT", "false"),
        createApplicationInput("unearnedIncome", "TRIBAL_PAYMENTS", "false"),
        createApplicationInput("unearnedIncomeCcap", "BENEFITS", "false"),
        createApplicationInput("unearnedIncomeCcap", "INSURANCE_PAYMENTS", "false"),
        createApplicationInput("unearnedIncomeCcap", "CONTRACT_FOR_DEED", "false"),
        createApplicationInput("unearnedIncomeCcap", "TRUST_MONEY", "false"),
        createApplicationInput("unearnedIncomeCcap", "HEALTH_CARE_REIMBURSEMENT", "false"),
        createApplicationInput("unearnedIncomeCcap", "INTEREST_DIVIDENDS", "false"),
        createApplicationInput("unearnedIncomeCcap", "OTHER_SOURCES", "false"),
        createApplicationInput("homeExpenses", "RENT", "false"),
        createApplicationInput("homeExpenses", "MORTGAGE", "false"),
        createApplicationInput("homeExpenses", "HOMEOWNERS_INSURANCE", "false"),
        createApplicationInput("homeExpenses", "REAL_ESTATE_TAXES", "false"),
        createApplicationInput("homeExpenses", "ASSOCIATION_FEES", "false"),
        createApplicationInput("homeExpenses", "ROOM_AND_BOARD", "false"),
        createApplicationInput("utilityPayments", "PHONE", "false"),
        createApplicationInput("utilityPayments", "GARBAGE_REMOVAL", "false"),
        createApplicationInput("utilityPayments", "ELECTRICITY", "false"),
        createApplicationInput("utilityPayments", "COOKING_FUEL", "false")
    );
  }

  @Test
  public void shouldMapYesToSelectedOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncome", "unearnedIncome",
            List.of("SOCIAL_SECURITY", "SSI", "CHILD_OR_SPOUSAL_SUPPORT"))
        .withPageData("unearnedIncomeCcap", "unearnedIncomeCcap",
            List.of("INSURANCE_PAYMENTS", "TRUST_MONEY"))
        .withPageData("homeExpenses", "homeExpenses",
            List.of("REAL_ESTATE_TAXES", "ASSOCIATION_FEES"))
        .withPageData("utilityPayments", "payForUtilities", List.of("HEATING", "PHONE"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("unearnedIncome", "SOCIAL_SECURITY", "true"),
        createApplicationInput("unearnedIncome", "SSI", "true"),
        createApplicationInput("unearnedIncome", "VETERANS_BENEFITS", "false"),
        createApplicationInput("unearnedIncome", "UNEMPLOYMENT", "false"),
        createApplicationInput("unearnedIncome", "WORKERS_COMPENSATION", "false"),
        createApplicationInput("unearnedIncome", "RETIREMENT", "false"),
        createApplicationInput("unearnedIncome", "CHILD_OR_SPOUSAL_SUPPORT", "true"),
        createApplicationInput("unearnedIncome", "TRIBAL_PAYMENTS", "false"),
        createApplicationInput("unearnedIncomeCcap", "BENEFITS", "false"),
        createApplicationInput("unearnedIncomeCcap", "INSURANCE_PAYMENTS", "true"),
        createApplicationInput("unearnedIncomeCcap", "CONTRACT_FOR_DEED", "false"),
        createApplicationInput("unearnedIncomeCcap", "TRUST_MONEY", "true"),
        createApplicationInput("unearnedIncomeCcap", "HEALTH_CARE_REIMBURSEMENT", "false"),
        createApplicationInput("unearnedIncomeCcap", "INTEREST_DIVIDENDS", "false"),
        createApplicationInput("unearnedIncomeCcap", "OTHER_SOURCES", "false"),
        createApplicationInput("homeExpenses", "RENT", "false"),
        createApplicationInput("homeExpenses", "MORTGAGE", "false"),
        createApplicationInput("homeExpenses", "HOMEOWNERS_INSURANCE", "false"),
        createApplicationInput("homeExpenses", "REAL_ESTATE_TAXES", "true"),
        createApplicationInput("homeExpenses", "ASSOCIATION_FEES", "true"),
        createApplicationInput("homeExpenses", "ROOM_AND_BOARD", "false"),
        createApplicationInput("utilityPayments", "PHONE", "true"),
        createApplicationInput("utilityPayments", "GARBAGE_REMOVAL", "false"),
        createApplicationInput("utilityPayments", "ELECTRICITY", "false"),
        createApplicationInput("utilityPayments", "COOKING_FUEL", "false")
    );
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }

  @NotNull
  private ApplicationInput createApplicationInput(String groupName, String name, String value) {
    return new ApplicationInput(groupName, name, List.of(value),
        ENUMERATED_SINGLE_VALUE);
  }
}
