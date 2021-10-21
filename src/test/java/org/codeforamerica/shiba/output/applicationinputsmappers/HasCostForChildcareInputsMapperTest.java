package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class HasCostForChildcareInputsMapperTest {

  private final HasCostForChildcareInputsMapper mapper = new HasCostForChildcareInputsMapper();

  @Test
  public void shouldMapToTrueForApplicantLivingAlone() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withApplicantPrograms(List.of("CCAP"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());

    applicationData = new TestApplicationDataBuilder()
        .withPageData("jobSearch", "currentlyLookingForJob", List.of("true"))
        .withApplicantPrograms(List.of("CCAP"))
        .build();

    result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());
  }

  @Test
  public void shouldMapEmptyForApplicantLivingAlone() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("false"))
        .withPageData("jobSearch", "currentlyLookingForJob", List.of("false"))
        .withApplicantPrograms(List.of("CCAP"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }

  @Test
  public void shouldMapTrueForHouseholdMember() {
    Subworkflow household = createHouseholdSubworkflow("CCAP");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withPageData("whoIsGoingToSchool", "whoIsGoingToSchool",
            List.of("some name " + household.get(0).getId()))
        .withApplicantPrograms(List.of("SNAP"))
        .build();
    applicationData.setSubworkflows(new Subworkflows(Map.of("household", household)));

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());
  }

  @Test
  public void shouldMapTrueForApplicantInHousehold() {
    Subworkflow household = createHouseholdSubworkflow("SNAP");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withPageData("whoIsGoingToSchool", "whoIsGoingToSchool", List.of("some name applicant"))
        .withApplicantPrograms(List.of("CCAP"))
        .build();
    applicationData.setSubworkflows(new Subworkflows(Map.of("household", household)));

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(createApplicationInput());
  }

  @Test
  public void shouldMapEmptyForHousehold() {
    Subworkflow household = createHouseholdSubworkflow("CCAP");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("goingToSchool", "goingToSchool", List.of("true"))
        .withPageData("whoIsGoingToSchool", "whoIsGoingToSchool", List.of("some name applicant"))
        .withApplicantPrograms(List.of("SNAP"))
        .build();
    applicationData.setSubworkflows(new Subworkflows(Map.of("household", household)));

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }


  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }

  @NotNull
  private Subworkflow createHouseholdSubworkflow(String program) {
    return new Subworkflow(List.of(PagesDataBuilder.build(List.of(
        new PageDataBuilder("householdMemberInfo",
            Map.of("firstName", List.of("Daria"),
                "lastName", List.of("Agàta"),
                "dateOfBirth", List.of("5", "6", "1978"),
                "maritalStatus", List.of("Never married"),
                "sex", List.of("Female"),
                "livedInMnWholeLife", List.of("Yes"),
                "relationship", List.of("housemate"),
                "programs", List.of(program),
                "ssn", List.of("123121234")))))));
  }


  @NotNull
  private ApplicationInput createApplicationInput() {
    return new ApplicationInput(
        "ccapHasCostsForChildCare",
        "ccapHasCostsForChildCare",
        List.of("true"),
        ApplicationInputType.ENUMERATED_SINGLE_VALUE
    );
  }
}
