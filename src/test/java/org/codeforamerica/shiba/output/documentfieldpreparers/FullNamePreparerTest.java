package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.inputconditions.ValueMatcher;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FullNamePreparerTest {

  private ApplicationData applicationData;
  private ApplicationConfiguration applicationConfiguration;
  private FullNamePreparer fullNamePreparer;

  @BeforeEach
  void setUp() {
    applicationData = new ApplicationData();
    applicationConfiguration = new ApplicationConfiguration();
    fullNamePreparer = new FullNamePreparer(applicationConfiguration);
  }

  @Test
  void mapsFullNamesForHouseholdAndJobIndividualsWithoutIdentifiers() {
    Condition condition1 = new Condition("selfEmployment", "selfEmployment", "true",
        ValueMatcher.CONTAINS, null, null);
    String prefix1 = "selfEmployment";
    Condition condition2 = new Condition("selfEmployment", "selfEmployment", "false",
        ValueMatcher.CONTAINS, null, null);
    String prefix2 = "nonSelfEmployment";
    Map<String, Condition> addedScope = Map.of(prefix1, condition1, prefix2, condition2);
    PageGroupConfiguration jobsGroup = new PageGroupConfiguration();
    jobsGroup.setAddedScope(addedScope);

    applicationConfiguration.setPageGroups(Map.of("jobs", jobsGroup));

    new TestApplicationDataBuilder(applicationData)
        .withSubworkflow("jobs",
            new PagesDataBuilder()
                .withPageData("householdSelectionForIncome", "whoseJobIsIt",
                    "Fake Person applicant"),
            new PagesDataBuilder()
                .withPageData("householdSelectionForIncome",
                    "whoseJobIsIt", "Different Person some-random-guid-1234")
                .withPageData("selfEmployment", "selfEmployment", "false")
        ).build();

    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(fullNamePreparer
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new DocumentField(
                "householdSelectionForIncome",
                "employeeFullName",
                List.of("Fake Person"),
                DocumentFieldType.SINGLE_VALUE,
                0
            ),
            new DocumentField(
                "householdSelectionForIncome",
                "employeeFullName",
                List.of("Different Person"),
                DocumentFieldType.SINGLE_VALUE,
                1
            ),
            new DocumentField(
                "nonSelfEmployment_householdSelectionForIncome",
                "employeeFullName",
                List.of("Different Person"),
                DocumentFieldType.SINGLE_VALUE,
                0
            )
        );
  }

  @Test
  void returnsEmptyListWhenJobsSubworkflowIsntThere() {
    new TestApplicationDataBuilder(applicationData)
        .withSubworkflow("otherSubworkflow", new PagesDataBuilder()
            .withPageData("otherPage", "uselessInput", "unimportantAnswer"));

    Application application = Application.builder().applicationData(applicationData).build();
    assertThat(
        fullNamePreparer.prepareDocumentFields(application, null, Recipient.CLIENT, null))
        .isEqualTo(emptyList());
  }
}
