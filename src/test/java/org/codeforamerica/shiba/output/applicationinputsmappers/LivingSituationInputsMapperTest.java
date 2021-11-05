package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
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

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "livingSituation",
            "derivedLivingSituation",
            List.of("TEMPORARILY_WITH_FRIENDS_OR_FAMILY"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapPlaceNotMeantForHousingAndAddCounty() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("livingSituation", "livingSituation",
            List.of("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING"))
        .withPageData("identifyCounty", "county", "Hennepin")
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "livingSituation",
            "derivedLivingSituation",
            List.of("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ),
        new DocumentField(
            "livingSituation",
            "county",
            List.of("Hennepin"),
            DocumentFieldType.SINGLE_VALUE
        )
    );
  }

  @Test
  public void shouldMapUnansweredToUnknown() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("livingSituation", "livingSituation",
            List.of())
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "livingSituation",
            "derivedLivingSituation",
            List.of("UNKNOWN"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapOneToOne() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("livingSituation", "livingSituation",
            List.of("HOTEL_OR_MOTEL"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "livingSituation",
            "derivedLivingSituation",
            List.of("HOTEL_OR_MOTEL"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }
}
