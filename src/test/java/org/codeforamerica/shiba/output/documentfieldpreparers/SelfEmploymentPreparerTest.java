package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class SelfEmploymentPreparerTest {

  private final SelfEmploymentPreparer selfEmploymentPreparer = new SelfEmploymentPreparer();

  @Test
  void shouldMapTrueIfSelfEmployedJobExists() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("jobs",
            new PagesDataBuilder().withHourlyJob("false", "10", "10"),
            new PagesDataBuilder().withNonHourlyJob("true", "10", "EVERY_WEEK"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentPreparer
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new DocumentField(
                "employee",
                "selfEmployed",
                List.of("true"),
                DocumentFieldType.SINGLE_VALUE
            ),
            new DocumentField(
                "employee",
                "selfEmployedGrossMonthlyEarnings",
                List.of("see question 9"),
                DocumentFieldType.SINGLE_VALUE
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

    assertThat(selfEmploymentPreparer
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new DocumentField(
                "employee",
                "selfEmployed",
                List.of("false"),
                DocumentFieldType.SINGLE_VALUE
            ),
            new DocumentField(
                "employee",
                "selfEmployedGrossMonthlyEarnings",
                List.of(""),
                DocumentFieldType.SINGLE_VALUE
            )
        );
  }

  @Test
  void shouldMapEmptyIfNoJobs() {
    ApplicationData applicationData = new ApplicationData();
    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentPreparer
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker()))
        .isEmpty();
  }

}
