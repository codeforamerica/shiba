package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class OneToManyApplicationInputsMapperTest {

  private final OneToManyApplicationInputsMapper mapper = new OneToManyApplicationInputsMapper();

  @Test
  public void shouldMapNoneOfTheAboveToNoForAllOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
        .withPageData("unearnedIncomeCcap", "unearnedIncomeCcap",
            List.of("NO_UNEARNED_INCOME_CCAP_SELECTED"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput("unearnedIncome", "SOCIAL_SECURITY", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "SSI", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "VETERANS_BENEFITS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "UNEMPLOYMENT", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "WORKERS_COMPENSATION", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "RETIREMENT", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "CHILD_OR_SPOUSAL_SUPPORT", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "TRIBAL_PAYMENTS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "BENEFITS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "INSURANCE_PAYMENTS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "CONTRACT_FOR_DEED", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "TRUST_MONEY", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "HEALTH_CARE_REIMBURSEMENT", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "INTEREST_DIVIDENDS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "OTHER_SOURCES", List.of("false"),
            ENUMERATED_SINGLE_VALUE)
    );
  }

  @Test
  public void shouldMapYesToSelectedOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncome", "unearnedIncome",
            List.of("SOCIAL_SECURITY", "SSI", "CHILD_OR_SPOUSAL_SUPPORT"))
        .withPageData("unearnedIncomeCcap", "unearnedIncomeCcap",
            List.of("INSURANCE_PAYMENTS", "TRUST_MONEY"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput("unearnedIncome", "SOCIAL_SECURITY", List.of("true"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "SSI", List.of("true"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "VETERANS_BENEFITS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "UNEMPLOYMENT", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "WORKERS_COMPENSATION", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "RETIREMENT", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "CHILD_OR_SPOUSAL_SUPPORT", List.of("true"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncome", "TRIBAL_PAYMENTS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "BENEFITS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "INSURANCE_PAYMENTS", List.of("true"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "CONTRACT_FOR_DEED", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "TRUST_MONEY", List.of("true"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "HEALTH_CARE_REIMBURSEMENT", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "INTEREST_DIVIDENDS", List.of("false"),
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("unearnedIncomeCcap", "OTHER_SOURCES", List.of("false"),
            ENUMERATED_SINGLE_VALUE)
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
}
