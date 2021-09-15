package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class LivingSituationInputsMapperTest {

  private final LivingSituationInputsMapper mapper = new LivingSituationInputsMapper();

  @Test
  public void shouldMapTempLivingWithFriendsOrFamily() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("livingSituation", "livingSituation",
            List.of("TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "livingSituation",
            "derivedLivingSituation",
            List.of("TEMPORARILY_WITH_FRIENDS_OR_FAMILY"),
            ApplicationInputType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapUnansweredToUnknown() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("livingSituation", "livingSituation",
            List.of())
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "livingSituation",
            "derivedLivingSituation",
            List.of("UNKNOWN"),
            ApplicationInputType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapOneToOne() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("livingSituation", "livingSituation",
            List.of("HOTEL_OR_MOTEL"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "livingSituation",
            "derivedLivingSituation",
            List.of("HOTEL_OR_MOTEL"),
            ApplicationInputType.ENUMERATED_SINGLE_VALUE
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
