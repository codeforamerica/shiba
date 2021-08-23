package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

public class StudentFullNameInputsMapperTest {

  StudentFullNameInputsMapper mapper = new StudentFullNameInputsMapper();

  @Test
  void shouldCreateListOfStudentFullNames() {
    ApplicationData applicationData = new ApplicationData();
    PagesData pagesData = new PagesData();
    PageData studentsPage = new PageData();
    studentsPage.put("whoIsGoingToSchool", InputData.builder()
        .value(List.of("studentAFirstName studentALastName 939dc33-d13a-4cf0-9093-309293k3",
            "studentBFirstName studentBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d"))
        .build());
    pagesData.put("whoIsGoingToSchool", studentsPage);
    PageData childrenWhoNeedCare = new PageData();
    childrenWhoNeedCare.put("whoNeedsChildCare", InputData.builder()
        .value(List.of("studentBFirstName studentBLastName b99f3f7e-d13a-4cf0-9093-23ccdba2a64d"))
        .build());
    pagesData.put("childrenInNeedOfCare", childrenWhoNeedCare);
    applicationData.setPagesData(pagesData);

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).contains(
        new ApplicationInput(
            "whoIsGoingToSchool",
            "fullName",
            List.of("studentBFirstName studentBLastName"),
            ApplicationInputType.SINGLE_VALUE,
            0
        ));
  }
}
