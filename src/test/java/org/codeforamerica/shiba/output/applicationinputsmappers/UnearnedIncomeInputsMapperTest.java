package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class UnearnedIncomeInputsMapperTest {

  private final UnearnedIncomeInputsMapper mapper = new UnearnedIncomeInputsMapper();

  @Test
  public void shouldMapNoneOfTheAboveToNoForAllOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncome", "unearnedIncome", List.of("NO_UNEARNED_INCOME_SELECTED"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
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
        createApplicationInput("unearnedIncome", "TRIBAL_PAYMENTS", "false")
    );
  }

  @Test
  public void shouldMapYesToSelectedOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("unearnedIncome", "unearnedIncome",
            List.of("SOCIAL_SECURITY", "SSI", "CHILD_OR_SPOUSAL_SUPPORT"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
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
        createApplicationInput("unearnedIncome", "TRIBAL_PAYMENTS", "false")
    );
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }

}
