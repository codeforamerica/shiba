package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class FullNameInputsMapperTest {
    FullNameInputsMapper fullNameInputsMapper = new FullNameInputsMapper();
    PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();
    private final ApplicationData applicationData = new ApplicationData();

    @Test
    void mapsFullNamesForHouseholdAndJobIndividualsWithoutIdentifiers() {
        Subworkflow householdMember1Subworkflow = new Subworkflow();
        householdMember1Subworkflow.add(pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdSelectionForIncome", Map.of(
                        "whoseJobIsIt", List.of("Fake Person applicant")
                ))
        )));
        householdMember1Subworkflow.add(pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdSelectionForIncome", Map.of(
                        "whoseJobIsIt", List.of("Different Person some-random-guid-1234")
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

        assertThat(fullNameInputsMapper.map(application, Recipient.CLIENT)).containsExactlyInAnyOrder(
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
                )
        );
}

    @Test
    void returnsEmptyListWhenJobsSubworkflowIsntThere() {
        Subworkflow householdMember1Subworkflow = new Subworkflow();
        householdMember1Subworkflow.add(pagesDataBuilder.build(List.of(
                new PageDataBuilder("otherPage", Map.of(
                        "uselessInput", List.of("unimportantAnswer")
                ))
        )));
        Subworkflows subworkflows = new Subworkflows(Map.of("otherSubworkflow", householdMember1Subworkflow));

        applicationData.setSubworkflows(subworkflows);
        Application application = Application.builder().applicationData(applicationData).build();
        assertThat(fullNameInputsMapper.map(application, Recipient.CLIENT)).isEqualTo(emptyList());
    }
}
