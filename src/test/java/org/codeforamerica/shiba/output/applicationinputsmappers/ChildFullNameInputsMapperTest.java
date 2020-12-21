package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ChildFullNameInputsMapperTest {
    ChildFullNameInputsMapper mapper = new ChildFullNameInputsMapper();

    @Test
    void shouldCreateListOfChildFullNames() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData childrenInNeedOfCarePage = new PageData();
        childrenInNeedOfCarePage.put("whoNeedsChildCare", InputData.builder()
                .value(List.of("childAFirstName childALastName 939dc33-d13a-4cf0-9093-309293k3", "childBFirstName childBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d"))
                .build());
        pagesData.put("childrenInNeedOfCare", childrenInNeedOfCarePage);
        applicationData.setPagesData(pagesData);

        List<ApplicationInput> result = mapper.map(Application.builder()
                .applicationData(applicationData)
                .build(), null, null);

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
