package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class HouseholdUsCitizenMapperTest {

  HouseholdUsCitizenMapper mapper = new HouseholdUsCitizenMapper();

  @Test
  void shouldParseTrueOrFalseForHouseholdMemberUsCitizenship() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.getSubworkflows().addIteration("household", new PagesData());
    UUID householdMemberID = applicationData.getSubworkflows().get("household").get(0).getId();

    new TestApplicationDataBuilder(applicationData)
        .withPageData("whoIsNonCitizen", "whoIsNonCitizen",
            List.of("personAFirstName personALastName applicant",
                "personBFirstName personBLastName " + householdMemberID));

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEqualTo(List.of(
        new DocumentField(
            "usCitizen",
            "isUsCitizen",
            List.of("false"),
            DocumentFieldType.SINGLE_VALUE,
            null
        ),
        new DocumentField(
            "usCitizen",
            "isUsCitizen",
            List.of("false"),
            DocumentFieldType.SINGLE_VALUE,
            0
        )
    ));
  }
}
