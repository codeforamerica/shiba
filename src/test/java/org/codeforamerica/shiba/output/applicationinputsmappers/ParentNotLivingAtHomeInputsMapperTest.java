package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class ParentNotLivingAtHomeInputsMapperTest {

  ParentNotLivingAtHomeInputsMapper mapper = new ParentNotLivingAtHomeInputsMapper();

  @Test
  void shouldCreateListOfParentNamesForCorrespondingChildren() {
    List<String> childrenInNeedOfCare = List.of(
        "childAFirstName childALastName 939dc33-d13a-4cf0-9093-309293k3",
        "childBFirstName childBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d",
        "childCFirstName childCLastName b99f3f7e-d13a-4cf0-9093-4092384");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("childrenInNeedOfCare", "whoNeedsChildCare", childrenInNeedOfCare)
        .withPageData("parentNotAtHomeNames", "whatAreTheParentsNames",
            List.of("parentAName", "parentCName"))
        .withPageData("parentNotAtHomeNames", "childIdMap",
            List.of("939dc33-d13a-4cf0-9093-309293k3", "b99f3f7e-d13a-4cf0-9093-4092384"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).contains(
        new ApplicationInput(
            "custodyArrangement",
            "parentNotAtHomeName",
            List.of("parentAName"),
            ApplicationInputType.SINGLE_VALUE,
            0),
        new ApplicationInput(
            "custodyArrangement",
            "childFullName",
            List.of("childAFirstName childALastName"),
            ApplicationInputType.SINGLE_VALUE,
            0
        ),
        new ApplicationInput(
            "custodyArrangement",
            "parentNotAtHomeName",
            List.of("parentCName"),
            ApplicationInputType.SINGLE_VALUE,
            1),
        new ApplicationInput(
            "custodyArrangement",
            "childFullName",
            List.of("childCFirstName childCLastName"),
            ApplicationInputType.SINGLE_VALUE,
            1
        ));
  }

  @Test
  void shouldCreateEmptyListWhenPageDataIsNull() {
    List<String> childrenInNeedOfCare = List.of(
        "childAFirstName childALastName 939dc33-d13a-4cf0-9093-309293k3",
        "childBFirstName childBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d",
        "childCFirstName childCLastName b99f3f7e-d13a-4cf0-9093-4092384");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("childrenInNeedOfCare", "whoNeedsChildCare", childrenInNeedOfCare).build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }
}
