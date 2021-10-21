package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.inputconditions.ValueMatcher;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HourlyJobInputsMapperTest {

  private final ApplicationConfiguration applicationConfiguration = mock(
      ApplicationConfiguration.class);
  private final PageGroupConfiguration jobGroup = new PageGroupConfiguration();
  private final SubworkflowIterationScopeTracker scopeTracker = new SubworkflowIterationScopeTracker();
  private final HourlyJobInputsMapper mapper = new HourlyJobInputsMapper(
      new GrossMonthlyIncomeParser(), applicationConfiguration);

  @BeforeEach
  void setUp() {
    jobGroup.setAddedScope(Map.of("nonSelfEmployment",
        new Condition("selfEmployment", "selfEmployment", "false", ValueMatcher.CONTAINS)));
    when(applicationConfiguration.getPageGroups())
        .thenReturn(Map.of("jobs", jobGroup));
  }

  @Test
  public void shouldCreateInputsForHourlyOnly() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setSubworkflows(
        new Subworkflows(Map.of("jobs", new Subworkflow(List.of(
            PagesDataBuilder.build(List.of(
                new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("false"))),
                new PageDataBuilder("payPeriod", Map.of("payPeriod", List.of("EVERY_WEEK"))),
                new PageDataBuilder("incomePerPayPeriod",
                    Map.of("incomePerPayPeriod", List.of("1.1")))
            )),
            PagesDataBuilder.build(List.of(
                new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("true"))),
                new PageDataBuilder("hourlyWage", Map.of("hourlyWage", List.of("10"))),
                new PageDataBuilder("hoursAWeek", Map.of("hoursAWeek", List.of("12")))
            ))
        )))));
    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, scopeTracker);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "payPeriod",
            "payPeriod",
            List.of("Hourly"),
            SINGLE_VALUE,
            1
        ));
  }

  @Test
  public void shouldCreateMultipleInputsForHourlyForCertainPops() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setSubworkflows(
        new Subworkflows(Map.of("jobs", new Subworkflow(List.of(
            PagesDataBuilder.build(
                List.of( // Hourly payPeriod iteration 0, nonSelfEmployed hourly pay period N/A
                    new PageDataBuilder("selfEmployment",
                        Map.of("selfEmployment", List.of("true"))),
                    new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("true"))),
                    new PageDataBuilder("hourlyWage", Map.of("hourlyWage", List.of("11"))),
                    new PageDataBuilder("hoursAWeek", Map.of("hoursAWeek", List.of("13")))
                )),
            PagesDataBuilder.build(List.of( // Hourly payPeriod N/A, nonSelfEmployed pay period 0
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("false"))),
                new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("false"))),
                new PageDataBuilder("payPeriod", Map.of("payPeriod", List.of("EVERY_WEEK"))),
                new PageDataBuilder("incomePerPayPeriod",
                    Map.of("incomePerPayPeriod", List.of("1.1")))
            )),
            PagesDataBuilder.build(List.of( // Hourly payPeriod 1, nonSelfEmployed pay period 1
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("false"))),
                new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("true"))),
                new PageDataBuilder("hourlyWage", Map.of("hourlyWage", List.of("10"))),
                new PageDataBuilder("hoursAWeek", Map.of("hoursAWeek", List.of("12")))
            ))
        )))));
    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), Document.CERTAIN_POPS, null, scopeTracker);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "payPeriod",
            "payPeriod",
            "Hourly",
            SINGLE_VALUE,
            0
        ),
        new ApplicationInput(
            "payPeriod",
            "payPeriod",
            "Hourly",
            SINGLE_VALUE,
            2
        ),
        new ApplicationInput(
            "nonSelfEmployment_payPeriod",
            "payPeriod",
            "Hourly",
            SINGLE_VALUE,
            1
        ));
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
