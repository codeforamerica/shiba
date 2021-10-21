package org.codeforamerica.shiba.application.parsers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.NonHourlyJobIncomeInformation;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class GrossMonthlyIncomeParserTest {

  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser = new GrossMonthlyIncomeParser();

  @Test
  void shouldProvideHourlyJobInformation() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("false", "12", "30").build(),
            new PagesDataBuilder().withHourlyJob("false", "6", "45").build())
        .build();

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).contains(
        new HourlyJobIncomeInformation("12", "30", 0,
            applicationData.getSubworkflows().get("jobs").get(0)),
        new HourlyJobIncomeInformation("6", "45", 1,
            applicationData.getSubworkflows().get("jobs").get(1))
    );
  }

  @Test
  void shouldNotProvideJobInformationForJobsWithInsufficientInformationForCalculation() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("false", "", "").build(),
            new PagesDataBuilder().withNonHourlyJob("false", "", "").build())
        .build();

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).isEmpty();
  }

  @Test
  void shouldNotIncludeGrossMonthlyIncomeWhenJobsInformationIsNotAvailable() {
    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(new ApplicationData());

    assertThat(jobIncomeInformation).isEmpty();
  }

  @Test
  void shouldProvideNonHourlyJobInformation() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs", new PagesDataBuilder()
            .withNonHourlyJob("false", "1.1", "EVERY_WEEK").build())
        .build();

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).contains(
        new NonHourlyJobIncomeInformation("EVERY_WEEK", "1.1", 0,
            applicationData.getSubworkflows().get("jobs").get(0))
    );
  }

  @Test
  void shouldReturnNonHourlyJobInformationIfHourlyJobPageIsNotAvailable() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs", new PagesDataBuilder()
            .withNonHourlyJob("false", "1.1", "EVERY_WEEK").build())
        .build();

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).contains(
        new NonHourlyJobIncomeInformation("EVERY_WEEK", "1.1", 0,
            applicationData.getSubworkflows().get("jobs").get(0))
    );
  }
}
