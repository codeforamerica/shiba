package org.codeforamerica.shiba.application.parsers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.NonHourlyJobIncomeInformation;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrossMonthlyIncomeParserTest {

  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser = new GrossMonthlyIncomeParser();
  private ApplicationData applicationData;

  @BeforeEach
  void setUp() {
    applicationData = new ApplicationData();
  }

  @Test
  void shouldProvideHourlyJobInformation() {
    Subworkflow subworkflow = new Subworkflow();
    subworkflow.add(PagesDataBuilder.build(List.of(
        new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("true"))),
        new PageDataBuilder("hourlyWage", Map.of("hourlyWage", List.of("12"))),
        new PageDataBuilder("hoursAWeek", Map.of("hoursAWeek", List.of("30")))
    )));
    subworkflow.add(PagesDataBuilder.build(List.of(
        new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("true"))),
        new PageDataBuilder("hourlyWage", Map.of("hourlyWage", List.of("6"))),
        new PageDataBuilder("hoursAWeek", Map.of("hoursAWeek", List.of("45")))
    )));
    Subworkflows subworkflows = new Subworkflows();
    subworkflows.put("jobs", subworkflow);

    applicationData.setSubworkflows(subworkflows);

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).contains(
        new HourlyJobIncomeInformation("12", "30", 0, subworkflow.get(0)),
        new HourlyJobIncomeInformation("6", "45", 1, subworkflow.get(1))
    );
  }

  @Test
  void shouldNotProvideJobInformationForJobsWithInsufficientInformationForCalculation() {
    PagesData hourlyJobPagesData = PagesDataBuilder.build(List.of(
        new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("true"))),
        new PageDataBuilder("hourlyWage", Map.of("hourlyWage", List.of(""))),
        new PageDataBuilder("hoursAWeek", Map.of("hoursAWeek", List.of("")))
    ));

    PagesData nonHourlyJobPagesData = PagesDataBuilder.build(List.of(
        new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("false"))),
        new PageDataBuilder("payPeriod", Map.of("payPeriod", List.of(""))),
        new PageDataBuilder("incomePerPayPeriod", Map.of("incomePerPayPeriod", List.of(""))),
        new PageDataBuilder("last30DaysJobIncome", Map.of("last30DaysJobIncome", List.of("")))
    ));

    Subworkflow subworkflow = new Subworkflow();
    Subworkflows subworkflows = new Subworkflows();
    subworkflow.add(hourlyJobPagesData);
    subworkflow.add(nonHourlyJobPagesData);
    subworkflows.put("jobs", subworkflow);
    applicationData.setSubworkflows(subworkflows);

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).isEmpty();
  }

  @Test
  void shouldNotIncludeGrossMonthlyIncomeWhenJobsInformationIsNotAvailable() {
    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).isEmpty();
  }

  @Test
  void shouldProvideNonHourlyJobInformation() {
    Subworkflow subworkflow = new Subworkflow();
    subworkflow.add(PagesDataBuilder.build(List.of(
        new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("false"))),
        new PageDataBuilder("payPeriod", Map.of("payPeriod", List.of("EVERY_WEEK"))),
        new PageDataBuilder("incomePerPayPeriod", Map.of("incomePerPayPeriod", List.of("1.1")))
    )));

    Subworkflows subworkflows = new Subworkflows();
    subworkflows.put("jobs", subworkflow);
    applicationData.setSubworkflows(subworkflows);

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).contains(
        new NonHourlyJobIncomeInformation("EVERY_WEEK", "1.1", 0, subworkflow.get(0))
    );
  }

  @Test
  void shouldReturnNonHourlyJobInformationIfHourlyJobPageIsNotAvailable() {
    Subworkflow subworkflow = new Subworkflow();
    subworkflow.add(PagesDataBuilder.build(List.of(
        new PageDataBuilder("payPeriod", Map.of("payPeriod", List.of("EVERY_WEEK"))),
        new PageDataBuilder("incomePerPayPeriod", Map.of("incomePerPayPeriod", List.of("1.1")))
    )));
    applicationData.setSubworkflows(new Subworkflows(Map.of("jobs", subworkflow)));

    List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser
        .parse(applicationData);

    assertThat(jobIncomeInformation).contains(
        new NonHourlyJobIncomeInformation("EVERY_WEEK", "1.1", 0, subworkflow.get(0))
    );
  }
}
