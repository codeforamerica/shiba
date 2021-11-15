package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class HouseholdPregnancyPreparerTest {

  HouseholdPregnancyPreparer preparer = new HouseholdPregnancyPreparer();

  @Test
  void shouldJoinAllNamesTogether() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("whoIsPregnant", "whoIsPregnant",
            List.of("personAFirstName personALastName applicant",
                "personBFirstName personBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d")).build();

    List<DocumentField> result = preparer.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null);

    assertThat(result).contains(new DocumentField(
        "householdPregnancy",
        "householdPregnancy",
        List.of("personAFirstName personALastName, personBFirstName personBLastName"),
        DocumentFieldType.SINGLE_VALUE,
        null
    ));
  }
}
