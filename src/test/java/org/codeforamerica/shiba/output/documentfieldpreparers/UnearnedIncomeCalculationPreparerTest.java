package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInputSingleValue;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class UnearnedIncomeCalculationPreparerTest {

  private final UnearnedIncomeCalculationPreparer preparer = new UnearnedIncomeCalculationPreparer();

  @Test
  public void shouldListOutIndividualAmounts() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncomeSources", "socialSecurityAmount",
            List.of("1,234"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnlyOnce(
        createApplicationInputSingleValue("unearnedIncomeSource", "socialSecurityAmount", "1234")
    );
  }

  @Test
  public void shouldListOutTotalHouseHoldAmounts() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("socialSecurityIncomeSource", "socialSecurityAmount",
            List.of("2,000", "3,000", "4,000"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnlyOnce(
        createApplicationInputSingleValue("unearnedIncomeSource", "socialSecurityAmount", "9000")
    );
  }

}
