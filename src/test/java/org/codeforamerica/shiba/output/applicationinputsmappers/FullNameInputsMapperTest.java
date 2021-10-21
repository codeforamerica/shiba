package org.codeforamerica.shiba.output.applicationinputsmappers;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.inputconditions.ValueMatcher;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;

public class FullNameInputsMapperTest {

  private final ApplicationData applicationData = new ApplicationData();
  ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
  FullNameInputsMapper fullNameInputsMapper = new FullNameInputsMapper(applicationConfiguration);

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

    Subworkflow householdMember1Subworkflow = new Subworkflow();
    householdMember1Subworkflow.add(PagesDataBuilder.build(List.of(
        new PageDataBuilder("householdSelectionForIncome", Map.of(
            "whoseJobIsIt", List.of("Fake Person applicant")
        ))
    )));
    householdMember1Subworkflow.add(PagesDataBuilder.build(List.of(
        new PageDataBuilder("householdSelectionForIncome", Map.of(
            "whoseJobIsIt", List.of("Different Person some-random-guid-1234")
        )),
        new PageDataBuilder("selfEmployment", Map.of(
            "selfEmployment", List.of("false")
        ))
    )));

    Subworkflows subworkflows = new Subworkflows(
        Map.of(
            "jobs",
            householdMember1Subworkflow
        )
    );

    applicationData.setSubworkflows(subworkflows);
    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(fullNameInputsMapper
        .map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new ApplicationInput(
                "householdSelectionForIncome",
                "employeeFullName",
                List.of("Fake Person"),
                ApplicationInputType.SINGLE_VALUE,
                0
            ),
            new ApplicationInput(
                "householdSelectionForIncome",
                "employeeFullName",
                List.of("Different Person"),
                ApplicationInputType.SINGLE_VALUE,
                1
            ),
            new ApplicationInput(
                "nonSelfEmployment_householdSelectionForIncome",
                "employeeFullName",
                List.of("Different Person"),
                ApplicationInputType.SINGLE_VALUE,
                0
            )
        );
  }

  @Test
  void returnsEmptyListWhenJobsSubworkflowIsntThere() {
    Subworkflow householdMember1Subworkflow = new Subworkflow();
    householdMember1Subworkflow.add(PagesDataBuilder.build(List.of(
        new PageDataBuilder("otherPage", Map.of(
            "uselessInput", List.of("unimportantAnswer")
        ))
    )));
    Subworkflows subworkflows = new Subworkflows(
        Map.of("otherSubworkflow", householdMember1Subworkflow));

    applicationData.setSubworkflows(subworkflows);
    Application application = Application.builder().applicationData(applicationData).build();
    assertThat(fullNameInputsMapper.map(application, null, Recipient.CLIENT, null))
        .isEqualTo(emptyList());
  }
}
