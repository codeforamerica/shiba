package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdultRequestingChildcarePreparerTest {

  private AdultRequestingChildcarePreparer adultRequestingChildcarePreparer;

  @BeforeEach
  void setUp() {
    adultRequestingChildcarePreparer = new AdultRequestingChildcarePreparer();
  }

  @Test
  void shouldReturnEmptyListWhenLivingAlone() {
    ApplicationData appData = new TestApplicationDataBuilder()
        .withPageData("addHouseholdMembers", "addHouseholdMembers", "false").build();

    Application application = Application.builder().applicationData(appData).build();
    assertThat(new AdultRequestingChildcarePreparer()
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker()))
        .isEqualTo(emptyList());
  }

  @Test
  void shouldReturnEmptyListWithoutCCAP() {
    ApplicationData appData = new ApplicationData();

    Application application = Application.builder().applicationData(appData).build();
    assertThat(adultRequestingChildcarePreparer
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker()))
        .isEqualTo(emptyList());
  }

  @Test
  void shouldReturnListOfAdultsRequestingChildcareWhoAreWorking() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CCAP"),
                "firstName", "Jane",
                "lastName", "Testerson")),
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CCAP"),
                "firstName", "John",
                "lastName", "Testerson")))
        .withSubworkflow("jobs",
            new PagesDataBuilder()
                .withPageData("householdSelectionForIncome", "whoseJobIsIt",
                    "John Testerson 939dc33-d13a-4cf0-9093-309293k3")
                .withPageData("employersName", "employersName", "John's Job"),
            new PagesDataBuilder()
                .withPageData("householdSelectionForIncome", "whoseJobIsIt",
                    "Jane Testerson 939dc44-d14a-3cf0-9094-409294k4")
                .withPageData("employersName", Map.of("employersName", "Jane's Job")))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> result = adultRequestingChildcarePreparer
        .prepareDocumentFields(application, null, null, new SubworkflowIterationScopeTracker());

    assertThat(result).contains(
        new DocumentField(
            "adultRequestingChildcareWorking",
            "fullName",
            List.of("John Testerson"),
            SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "adultRequestingChildcareWorking",
            "employersName",
            List.of("John's Job"),
            SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "adultRequestingChildcareWorking",
            "fullName",
            List.of("Jane Testerson"),
            SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "adultRequestingChildcareWorking",
            "employersName",
            List.of("Jane's Job"),
            SINGLE_VALUE,
            1
        )
    );
  }

  @Test
  void shouldReturnListOfAdultsRequestingChildcareWhoAreLookingForWork() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CCAP"),
                "firstName", "Jane",
                "lastName", "Testerson")),
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CCAP"),
                "firstName", "John",
                "lastName", "Testerson")))
        .withPageData("whoIsLookingForAJob", "whoIsLookingForAJob",
            List.of("John Testerson 939dc33-d13a-4cf0-9093-309293k3",
                "Jane Testerson 939dc4-d13a-3cf0-9094-409293k4"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> result = adultRequestingChildcarePreparer
        .prepareDocumentFields(application, null, null, null);

    assertThat(result).contains(
        new DocumentField(
            "adultRequestingChildcareLookingForJob",
            "fullName",
            List.of("John Testerson"),
            SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "adultRequestingChildcareLookingForJob",
            "fullName",
            List.of("Jane Testerson"),
            SINGLE_VALUE,
            1
        )
    );
  }

  @Test
  void shouldReturnListOfAdultsRequestingChildcareWhoAreGoingToSchool() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("household",
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CCAP"),
                "firstName", List.of("Jane"),
                "lastName", List.of("Testerson"))),
            new PagesDataBuilder().withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CCAP"),
                "firstName", List.of("John"),
                "lastName", List.of("Testerson"))))
        .withPageData("whoIsGoingToSchool", "whoIsGoingToSchool",
            List.of("John Testerson 939dc33-d13a-4cf0-9093-309293k3",
                "Jane Testerson 939dc4-d13a-3cf0-9094-409293k4"))
        .build();

    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> result = adultRequestingChildcarePreparer
        .prepareDocumentFields(application, null, null, null);

    assertThat(result).contains(
        new DocumentField(
            "adultRequestingChildcareGoingToSchool",
            "fullName",
            List.of("John Testerson"),
            SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "adultRequestingChildcareGoingToSchool",
            "fullName",
            List.of("Jane Testerson"),
            SINGLE_VALUE,
            1
        )
    );
  }
}
