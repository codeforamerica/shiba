package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class MedicalExpensesInputsMapperTest {

  private final MedicalExpensesInputsMapper mapper = new MedicalExpensesInputsMapper();

  @Test
  public void shouldMapNoneOfTheAboveToNoneSelected() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("medicalExpenses", "medicalExpenses", List.of("NONE_OF_THE_ABOVE"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "medicalExpenses",
            "medicalExpensesSelection",
            List.of("NONE_SELECTED"),
            DocumentFieldType.SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapToOneSelected() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("medicalExpenses", "medicalExpenses", List.of("VISION_INSURANCE_PREMIUMS"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "medicalExpenses",
            "medicalExpensesSelection",
            List.of("ONE_SELECTED"),
            DocumentFieldType.SINGLE_VALUE
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
