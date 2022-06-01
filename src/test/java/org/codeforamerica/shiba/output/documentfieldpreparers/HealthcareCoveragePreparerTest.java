package org.codeforamerica.shiba.output.documentfieldpreparers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthcareCoveragePreparerTest {

  private final HealthcareCoveragePreparer preparer = new HealthcareCoveragePreparer();

  @Test
  public void shouldMapNoToFalseHealthcareCoverage() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("healthcareCoverage", "healthcareCoverage",
            List.of("NO"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "healthcareCoverage",
            "healthcareCoverage",
            List.of("false"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapNotSureToLeaveBlankHealthcareCoverage() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("healthcareCoverage", "healthcareCoverage",
            List.of("NOT_SURE"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).doesNotContain(
        new DocumentField(
            "healthcareCoverage",
            "healthcareCoverage",
            List.of("false"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ));
    assertThat(result).doesNotContain(
            new DocumentField(
                    "healthcareCoverage",
                    "healthcareCoverage",
                    List.of("true"),
                    DocumentFieldType.ENUMERATED_SINGLE_VALUE
            ));
  }

  @Test
  public void shouldMapYesToTrueHealthcareCoverage() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("healthcareCoverage", "healthcareCoverage",
            List.of("YES"))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).containsOnly(
        new DocumentField(
            "healthcareCoverage",
            "healthcareCoverage",
            List.of("true"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE
        ));
  }
}
