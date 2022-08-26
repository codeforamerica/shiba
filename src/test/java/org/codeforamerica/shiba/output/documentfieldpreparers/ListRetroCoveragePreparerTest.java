package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class ListRetroCoveragePreparerTest {

  ListRetroCoveragePreparer preparer = new ListRetroCoveragePreparer();
  TestApplicationDataBuilder applicationDataTest = new TestApplicationDataBuilder();

  @Test
  void preparesFieldsForEveryoneInHouseForRetroactiveCoverage() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("retroactiveCoverage", "retroactiveCoverageQuestion", "true")
        .withPageData("retroactiveCoverageSource", "retroactiveCoverageSourceQuestion", List.of(
            "Daria Agàta someGuid",
            "Jane Doe applicant",
            "Other Person notSpouse"))
        .withRetroCoverageForHouseHold()
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        
        new DocumentField(
            "retroactiveCoverage",
            "applicantName",
            List.of("Daria Agàta"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "retroactiveCoverage",
            "month",
            List.of("1"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "retroactiveCoverage",
            "applicantName",
            List.of("Jane Doe"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "retroactiveCoverage",
            "month",
            List.of("2"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "retroactiveCoverage",
            "applicantName",
            List.of("Other Person"),
            DocumentFieldType.SINGLE_VALUE,
            2
        ),
        new DocumentField(
            "retroactiveCoverage",
            "month",
            List.of("3"),
            DocumentFieldType.SINGLE_VALUE,
            2
        )));
  }
  
  @Test
  void preparesFieldsForApplicantOnlyInIndivialFlow() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withPageData("retroactiveCoverage", "retroactiveCoverageQuestion", "true")
        .withPageData("retroactiveCoverageTimePeriodIndividual", "retroactiveCoverageNumberMonths", "3")
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "retroactiveCoverage",
            "applicantName",
            List.of("Jane Doe"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "retroactiveCoverage",
            "month",
            List.of("3"),
            DocumentFieldType.SINGLE_VALUE,
            0
        )));
  }

  @Test
  void preparesNoFieldsIfRetroactiveCoverageIsFalse() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("retroactiveCoverage", "retroactiveCoverageQuestion", "false")
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of());
  }
  
  @Test
  void preparesFieldsForRetroactiveCoverageSelectedHouseHoldOnly() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("retroactiveCoverage", "retroactiveCoverageQuestion", "true")
        .withPageData("retroactiveCoverageSource", "retroactiveCoverageSourceQuestion", List.of(
            "Daria Agàta someGuid",
            "Other Person notSpouse"
        ))
        .withPageData("retroactiveCoverageTimePeriod", "retroactiveCoverageMap", List.of(
            "someGuid",
            "notSpouse"
            ))
        .withPageData("retroactiveCoverageTimePeriod", "retroactiveCoverageNumberMonths", List.of(
            "3",
            "2"
            ))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "retroactiveCoverage",
            "applicantName",
            List.of("Daria Agàta"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "retroactiveCoverage",
            "month",
            List.of("3"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "retroactiveCoverage",
            "applicantName",
            List.of("Other Person"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "retroactiveCoverage",
            "month",
            List.of("2"),
            DocumentFieldType.SINGLE_VALUE,
            1
        )));
  }
}
