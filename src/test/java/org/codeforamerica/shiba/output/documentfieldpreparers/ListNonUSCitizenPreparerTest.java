package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class ListNonUSCitizenPreparerTest {

  ListNonUSCitizenPreparer preparer = new ListNonUSCitizenPreparer();
  TestApplicationDataBuilder applicationDataTest = new TestApplicationDataBuilder();

  @Test
  void preparesFieldsForApplicantAndLiveInSpouseNamesWhenEveryoneInHouseNotUSCitizen() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("usCitizen", "isUsCitizen", "false")
        .withPageData("whoIsNonCitizen", "whoIsNonCitizen", List.of(
            "Daria Agàta someGuid",
            "Jane Doe applicant",
            "Other Person notSpouse"
        ))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "whoIsNonUsCitizen",
            "nameOfApplicantOrSpouse",
            List.of("Jane Doe"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "whoIsNonUsCitizen",
            "nameOfApplicantOrSpouse",
            List.of("Daria Agàta"),
            DocumentFieldType.SINGLE_VALUE,
            1
        )));
  }
  
  @Test
  void preparesFieldsForApplicantOnlyNotUSCitizen() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("usCitizen", "isUsCitizen", "false")
        .withPageData("whoIsNonCitizen", "whoIsNonCitizen", List.of(
            "Jane Doe applicant",
            "Other Person notSpouse"
        ))
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "whoIsNonUsCitizen",
            "nameOfApplicantOrSpouse",
            List.of("Jane Doe"),
            DocumentFieldType.SINGLE_VALUE,
            0
        )));
  }

  @Test
  void preparesNoFieldsIfEveryoneInHouseIsUsCitizen() {
    ApplicationData applicationData = applicationDataTest
        .withPersonalInfo()
        .withMultipleHouseholdMembers()
        .withPageData("usCitizen", "isUsCitizen", "true")
        .build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, Recipient.CASEWORKER);

    assertThat(result).isEqualTo(List.of());
  }
}
