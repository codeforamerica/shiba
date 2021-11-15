package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HourlyJobPreparerTest {

  private final ApplicationConfiguration applicationConfiguration = mock(
      ApplicationConfiguration.class);
  private final PageGroupConfiguration jobGroup = new PageGroupConfiguration();
  private HourlyJobPreparer preparer;

  @BeforeEach
  void setUp() {
    preparer = new HourlyJobPreparer(new GrossMonthlyIncomeParser());
    when(applicationConfiguration.getPageGroups()).thenReturn(Map.of("jobs", jobGroup));
  }

  @Test
  public void shouldCreateInputsForHourlyOnly() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withNonHourlyJob("false", "1.1", "EVERY_WEEK"),
            new PagesDataBuilder().withHourlyJob("false", "10", "12"))
        .build();
    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnly(
        new DocumentField("payPeriod", "payPeriod", List.of("Hourly"), SINGLE_VALUE, 1)
    );
  }

  @Test
  public void shouldCreateMultipleInputsForHourlyForCertainPops() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            // Hourly payPeriod iteration 0, nonSelfEmployed hourly pay period N/A
            new PagesDataBuilder().withHourlyJob("true", "11", "13"),
            // Hourly payPeriod N/A, nonSelfEmployed pay period 0
            new PagesDataBuilder().withNonHourlyJob("false", "1.1", "EVERY_WEEK"),
            // Hourly payPeriod 1, nonSelfEmployed pay period 1
            new PagesDataBuilder().withHourlyJob("false", "10", "12"))
        .build();
    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), Document.CERTAIN_POPS, null);

    assertThat(result).containsOnly(
        new DocumentField("payPeriod", "payPeriod", "Hourly", SINGLE_VALUE, 0),
        new DocumentField("payPeriod", "payPeriod", "Hourly", SINGLE_VALUE, 2),
        new DocumentField("nonSelfEmployment_payPeriod", "payPeriod", "Hourly", SINGLE_VALUE, 1)
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
