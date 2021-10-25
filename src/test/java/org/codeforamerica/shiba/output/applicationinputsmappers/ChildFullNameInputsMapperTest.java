package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class ChildFullNameInputsMapperTest {

  ChildFullNameInputsMapper mapper = new ChildFullNameInputsMapper();

  @Test
  void shouldCreateListOfChildFullNames() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("childrenInNeedOfCare", "whoNeedsChildCare",
            List.of("childAFirstName childALastName 939dc33-d13a-4cf0-9093-309293k3",
                "childBFirstName childBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).contains(
        new ApplicationInput(
            "childNeedsChildcare",
            "fullName",
            List.of("childAFirstName childALastName"),
            ApplicationInputType.SINGLE_VALUE,
            0),
        new ApplicationInput(
            "childNeedsChildcare",
            "fullName",
            List.of("childBFirstName childBLastName"),
            ApplicationInputType.SINGLE_VALUE,
            1
        ));
  }
}
