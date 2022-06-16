package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class OtherUnearnedIncomePreparerTest {

  private final OtherUnearnedIncomePreparer preparer = new OtherUnearnedIncomePreparer();

  @Test
  public void shouldMapNoneOfTheAboveToNoForAllOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("otherUnearnedIncome", "otherUnearnedIncome",
            List.of("NO_OTHER_UNEARNED_INCOME_SELECTED"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnly(
        createApplicationInput("otherUnearnedIncome", "BENEFITS", "false"),
        createApplicationInput("otherUnearnedIncome", "INSURANCE_PAYMENTS", "false"),
        createApplicationInput("otherUnearnedIncome", "CONTRACT_FOR_DEED", "false"),
        createApplicationInput("otherUnearnedIncome", "TRUST_MONEY", "false"),
        createApplicationInput("otherUnearnedIncome", "HEALTH_CARE_REIMBURSEMENT", "false"),
        createApplicationInput("otherUnearnedIncome", "INTEREST_DIVIDENDS", "false"),
        createApplicationInput("otherUnearnedIncome", "RENTAL_INCOME", "false"),
        createApplicationInput("otherUnearnedIncome", "OTHER_PAYMENTS", "false")
    );
  }

  @Test
  public void shouldMapYesToSelectedOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("otherUnearnedIncome", "otherUnearnedIncome",
            List.of("INSURANCE_PAYMENTS", "TRUST_MONEY", "RENTAL_INCOME"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnly(
        createApplicationInput("otherUnearnedIncome", "BENEFITS", "false"),
        createApplicationInput("otherUnearnedIncome", "INSURANCE_PAYMENTS", "true"),
        createApplicationInput("otherUnearnedIncome", "CONTRACT_FOR_DEED", "false"),
        createApplicationInput("otherUnearnedIncome", "TRUST_MONEY", "true"),
        createApplicationInput("otherUnearnedIncome", "HEALTH_CARE_REIMBURSEMENT", "false"),
        createApplicationInput("otherUnearnedIncome", "INTEREST_DIVIDENDS", "false"),
        createApplicationInput("otherUnearnedIncome", "RENTAL_INCOME", "true"),
        createApplicationInput("otherUnearnedIncome", "OTHER_PAYMENTS", "false")
    );
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).isEmpty();
  }

}
