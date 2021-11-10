package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class HomeExpensesPreparerTest {

  private final HomeExpensesPreparer preparer = new HomeExpensesPreparer();

  @Test
  public void shouldMapNoneOfTheAboveToNoForAllOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("homeExpenses", "homeExpenses", List.of("NONE_OF_THE_ABOVE"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnly(
        createApplicationInput("homeExpenses", "RENT", "false"),
        createApplicationInput("homeExpenses", "MORTGAGE", "false"),
        createApplicationInput("homeExpenses", "HOMEOWNERS_INSURANCE", "false"),
        createApplicationInput("homeExpenses", "REAL_ESTATE_TAXES", "false"),
        createApplicationInput("homeExpenses", "ASSOCIATION_FEES", "false"),
        createApplicationInput("homeExpenses", "ROOM_AND_BOARD", "false")
    );
  }

  @Test
  public void shouldMapYesToSelectedOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("homeExpenses", "homeExpenses",
            List.of("REAL_ESTATE_TAXES", "ASSOCIATION_FEES"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnly(
        createApplicationInput("homeExpenses", "RENT", "false"),
        createApplicationInput("homeExpenses", "MORTGAGE", "false"),
        createApplicationInput("homeExpenses", "HOMEOWNERS_INSURANCE", "false"),
        createApplicationInput("homeExpenses", "REAL_ESTATE_TAXES", "true"),
        createApplicationInput("homeExpenses", "ASSOCIATION_FEES", "true"),
        createApplicationInput("homeExpenses", "ROOM_AND_BOARD", "false")
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
