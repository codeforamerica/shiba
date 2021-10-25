package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class HouseholdPregnancyMapperTest {

  HouseholdPregnancyMapper mapper = new HouseholdPregnancyMapper();

  @Test
  void shouldJoinAllNamesTogether() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("whoIsPregnant", "whoIsPregnant",
            List.of("personAFirstName personALastName applicant",
                "personBFirstName personBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d")).build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).contains(new ApplicationInput(
        "householdPregnancy",
        "householdPregnancy",
        List.of("personAFirstName personALastName, personBFirstName personBLastName"),
        ApplicationInputType.SINGLE_VALUE,
        null
    ));
  }
}
