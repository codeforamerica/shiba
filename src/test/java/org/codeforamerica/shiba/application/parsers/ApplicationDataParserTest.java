package org.codeforamerica.shiba.application.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PAID_BY_THE_HOUR;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;

import java.util.List;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationDataParserTest {

  private TestApplicationDataBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new TestApplicationDataBuilder();
  }

  @Test
  void shouldReturnExistingValue() {
    String expectedValue = "1";
    ApplicationData applicationData = builder
        .withPageData("paidByTheHour", "paidByTheHour", List.of(expectedValue))
        .build();

    String value = ApplicationDataParser
        .getFirstValue(applicationData.getPagesData(), PAID_BY_THE_HOUR);

    assertThat(value).isEqualTo(expectedValue);
  }

  @Test
  void shouldReturnNullForMissingPageOrInput() {
    String expectedValue = "1";
    ApplicationData applicationData = builder
        .withPageData("shmaidByTheHour", "paidByTheHour", List.of(expectedValue))
        .build();
    String value = ApplicationDataParser
        .getFirstValue(applicationData.getPagesData(), PAID_BY_THE_HOUR);
    assertThat(value).isNull();

    applicationData = builder
        .withPageData("paidByTheHour", "shmaidByTheHour", List.of(expectedValue))
        .build();
    value = ApplicationDataParser.getFirstValue(applicationData.getPagesData(), PAID_BY_THE_HOUR);
    assertThat(value).isNull();
  }

  @Test
  void shouldReturnSubworkflowForExistingGroup() {
    ApplicationData applicationData = builder.withJobs().build();

    assertThat(ApplicationDataParser.getGroup(applicationData, JOBS)).isNotNull();
  }
}
