package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParentNotLivingAtHomeInputsMapperTest {
    ParentNotLivingAtHomeInputsMapper mapper = new ParentNotLivingAtHomeInputsMapper();

    @Test
    void shouldCreateListOfParentNamesForCorrespondingChildren() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData childrenInNeedOfCarePage = new PageData();
        childrenInNeedOfCarePage.put("whoNeedsChildCare", InputData.builder()
                .value(List.of("childAFirstName childALastName 939dc33-d13a-4cf0-9093-309293k3", "childBFirstName childBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d", "childCFirstName childCLastName b99f3f7e-d13a-4cf0-9093-4092384"))
                .build());
        PageData parentsNotLivingAtHome = new PageData();
        parentsNotLivingAtHome.put("whatAreTheParentsNames", InputData.builder()
                .value(List.of("parentAName", "parentCName"))
                .build());
        parentsNotLivingAtHome.put("childIdMap", InputData.builder()
                .value(List.of("939dc33-d13a-4cf0-9093-309293k3", "b99f3f7e-d13a-4cf0-9093-4092384"))
                .build());

        pagesData.put("childrenInNeedOfCare", childrenInNeedOfCarePage);
        pagesData.put("parentNotAtHomeNames", parentsNotLivingAtHome);
        applicationData.setPagesData(pagesData);

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
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData childrenInNeedOfCarePage = new PageData();
        childrenInNeedOfCarePage.put("whoNeedsChildCare", InputData.builder()
                .value(List.of("childAFirstName childALastName 939dc33-d13a-4cf0-9093-309293k3", "childBFirstName childBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d", "childCFirstName childCLastName b99f3f7e-d13a-4cf0-9093-4092384"))
                .build());

        pagesData.put("childrenInNeedOfCare", childrenInNeedOfCarePage);
        applicationData.setPagesData(pagesData);

        List<ApplicationInput> result = mapper.map(Application.builder()
                .applicationData(applicationData)
                .build(), null, null, null);

        assertThat(result).isEmpty();
    }
}
