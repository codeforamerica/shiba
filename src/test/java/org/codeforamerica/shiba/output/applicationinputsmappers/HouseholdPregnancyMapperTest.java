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

class HouseholdPregnancyMapperTest {
    HouseholdPregnancyMapper mapper = new HouseholdPregnancyMapper();

    @Test
    void shouldJoinAllNamesTogether() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData whoIsPregnantPage = new PageData();
        whoIsPregnantPage.put("whoIsPregnant", InputData.builder()
                .value(List.of("personAFirstName personALastName applicant", "personBFirstName personBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d"))
                .build());
        pagesData.put("whoIsPregnant", whoIsPregnantPage);
        applicationData.setPagesData(pagesData);

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