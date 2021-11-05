package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class EnergyAssistanceInputsMapperTest {

  private final EnergyAssistanceInputsMapper mapper = new EnergyAssistanceInputsMapper();

  @Test
  public void shouldMapTrue() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("energyAssistance", "energyAssistance", List.of("true"))
        .withPageData("energyAssistanceMoreThan20", "energyAssistanceMoreThan20", List.of("true"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "energyAssistanceGroup",
            "energyAssistanceInput",
            List.of("true"),
            ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapFalseForNoAssistance() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("energyAssistance", "energyAssistance", List.of("false"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "energyAssistanceGroup",
            "energyAssistanceInput",
            List.of("false"),
            ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapFalseForNoLessThan20() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("energyAssistance", "energyAssistance", List.of("true"))
        .withPageData("energyAssistanceMoreThan20", "energyAssistanceMoreThan20", List.of("false"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "energyAssistanceGroup",
            "energyAssistanceInput",
            List.of("false"),
            ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();

    applicationData = new TestApplicationDataBuilder()
        .withPageData("energyAssistance", "energyAssistance", List.of("true"))
        .build();

    result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }
}
