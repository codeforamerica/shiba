package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;

public class PayPeriodInputsMapperTest {

  private final PayPeriodInputsMapper mapper = new PayPeriodInputsMapper();

  @Test
  public void shouldCreateInputsForHourlyOnly() {
    PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();
    ApplicationData applicationData = new ApplicationData();
    applicationData.setSubworkflows(
        new Subworkflows(Map.of("jobs", new Subworkflow(List.of(
            pagesDataBuilder.build(List.of(
                new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("false"))),
                new PageDataBuilder("payPeriod", Map.of("payPeriod", List.of("EVERY_WEEK"))),
                new PageDataBuilder("incomePerPayPeriod",
                    Map.of("incomePerPayPeriod", List.of("1.1")))
            )),
            pagesDataBuilder.build(List.of(
                new PageDataBuilder("paidByTheHour", Map.of("paidByTheHour", List.of("true"))),
                new PageDataBuilder("incomePerPayPeriod",
                    Map.of("incomePerPayPeriod", List.of("1.1")))
            ))
        )))));
    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "payPeriod",
            "payPeriod",
            List.of("Hourly"),
            ApplicationInputType.SINGLE_VALUE,
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
