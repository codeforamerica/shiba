package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class SelfEmploymentInputsMapperTest {

  private final SelfEmploymentInputsMapper selfEmploymentInputsMapper = new SelfEmploymentInputsMapper();

  @Test
  void shouldMapTrueIfSelfEmployedJobExists() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("false", "10", "10"),
            new PagesDataBuilder().withNonHourlyJob("true", "10", "EVERY_WEEK"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentInputsMapper
        .map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new ApplicationInput(
                "employee",
                "selfEmployed",
                List.of("true"),
                ApplicationInputType.SINGLE_VALUE
            ),
            new ApplicationInput(
                "employee",
                "selfEmployedGrossMonthlyEarnings",
                List.of("see question 9"),
                ApplicationInputType.SINGLE_VALUE
            )
        );
  }

  @Test
  void shouldMapFalseIfSelfEmployedJobDoesntExists() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("false", "10", "10"),
            new PagesDataBuilder().withNonHourlyJob("false", "10", "EVERY_WEEK"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentInputsMapper
        .map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new ApplicationInput(
                "employee",
                "selfEmployed",
                List.of("false"),
                ApplicationInputType.SINGLE_VALUE
            ),
            new ApplicationInput(
                "employee",
                "selfEmployedGrossMonthlyEarnings",
                List.of(""),
                ApplicationInputType.SINGLE_VALUE
            )
        );
  }

  @Test
  void shouldMapEmptyIfNoJobs() {
    ApplicationData applicationData = new ApplicationData();
    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentInputsMapper
        .map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker()))
        .isEmpty();
  }

}
