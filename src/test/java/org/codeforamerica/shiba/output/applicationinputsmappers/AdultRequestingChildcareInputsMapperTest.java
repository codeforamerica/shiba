package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

class AdultRequestingChildcareInputsMapperTest {
    AdultRequestingChildcareInputsMapper adultRequestingChildcareInputsMapper = new AdultRequestingChildcareInputsMapper();
    PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();


    @Test
    void shouldReturnEmptyListWhenLivingAlone() {
        ApplicationData appData = new ApplicationData();
        appData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("addHouseholdMembers", Map.of("addHouseholdMembers", List.of("false")))
        )));

        Application application = Application.builder().applicationData(appData).build();
        assertThat(new AdultRequestingChildcareInputsMapper().map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker())).isEqualTo(emptyList());
    }

    @Test
    void shouldReturnEmptyListWithoutCCAP() {
        ApplicationData appData = new ApplicationData();

        Application application = Application.builder().applicationData(appData).build();
        assertThat(adultRequestingChildcareInputsMapper.map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker())).isEqualTo(emptyList());
    }

    @Test
    void shouldReturnListOfAdultsRequestingChildcareWhoAreWorking() {
        ApplicationData applicationData = new ApplicationData();
        Subworkflows subworkflows = applicationData.getSubworkflows();
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CCAP"),
                        "firstName", List.of("Jane"),
                        "lastName", List.of("Testerson")
                ))
        )));
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CCAP"),
                        "firstName", List.of("John"),
                        "lastName", List.of("Testerson")
                ))
        )));
        subworkflows.addIteration("jobs", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdSelectionForIncome", Map.of("whoseJobIsIt", List.of("John Testerson 939dc33-d13a-4cf0-9093-309293k3"))),
                new PageDataBuilder("employersName", Map.of("employersName", List.of("John's Job"))))
        ));
        subworkflows.addIteration("jobs", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdSelectionForIncome", Map.of("whoseJobIsIt", List.of("Jane Testerson 939dc44-d14a-3cf0-9094-409294k4"))),
                new PageDataBuilder("employersName", Map.of("employersName", List.of("Jane's Job"))))
        ));
        applicationData.setSubworkflows(subworkflows);
        Application application = Application.builder().applicationData(applicationData).build();

        List<ApplicationInput> result = adultRequestingChildcareInputsMapper.map(application, null, null, null);

        assertThat(result).contains(
                new ApplicationInput(
                        "adultRequestingChildcareWorking",
                        "fullName",
                        List.of("John Testerson"),
                        SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "adultRequestingChildcareWorking",
                        "employersName",
                        List.of("John's Job"),
                        SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "adultRequestingChildcareWorking",
                        "fullName",
                        List.of("Jane Testerson"),
                        SINGLE_VALUE,
                        1
                ),
                new ApplicationInput(
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
        ApplicationData applicationData = new ApplicationData();
        Subworkflows subworkflows = applicationData.getSubworkflows();
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CCAP"),
                        "firstName", List.of("Jane"),
                        "lastName", List.of("Testerson")
                ))
        )));
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CCAP"),
                        "firstName", List.of("John"),
                        "lastName", List.of("Testerson")
                ))
        )));

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("whoIsLookingForAJob", Map.of(
                        "whoIsLookingForAJob", List.of("John Testerson 939dc33-d13a-4cf0-9093-309293k3", "Jane Testerson 939dc4-d13a-3cf0-9094-409293k4")
                ))
        ));

        applicationData.setSubworkflows(subworkflows);
        applicationData.setPagesData(pagesData);
        Application application = Application.builder().applicationData(applicationData).build();

        List<ApplicationInput> result = adultRequestingChildcareInputsMapper.map(application, null, null, null);

        assertThat(result).contains(
                new ApplicationInput(
                        "adultRequestingChildcareLookingForJob",
                        "fullName",
                        List.of("John Testerson"),
                        SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
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
        ApplicationData applicationData = new ApplicationData();
        Subworkflows subworkflows = applicationData.getSubworkflows();
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CCAP"),
                        "firstName", List.of("Jane"),
                        "lastName", List.of("Testerson")
                ))
        )));
        subworkflows.addIteration("household", pagesDataBuilder.build(List.of(
                new PageDataBuilder("householdMemberInfo", Map.of(
                        "programs", List.of("SNAP", "CCAP"),
                        "firstName", List.of("John"),
                        "lastName", List.of("Testerson")
                ))
        )));

        PagesData pagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("whoIsGoingToSchool", Map.of(
                        "whoIsGoingToSchool", List.of("John Testerson 939dc33-d13a-4cf0-9093-309293k3", "Jane Testerson 939dc4-d13a-3cf0-9094-409293k4")
                ))
        ));

        applicationData.setSubworkflows(subworkflows);
        applicationData.setPagesData(pagesData);
        Application application = Application.builder().applicationData(applicationData).build();

        List<ApplicationInput> result = adultRequestingChildcareInputsMapper.map(application, null, null, null);

        assertThat(result).contains(
                new ApplicationInput(
                        "adultRequestingChildcareGoingToSchool",
                        "fullName",
                        List.of("John Testerson"),
                        SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "adultRequestingChildcareGoingToSchool",
                        "fullName",
                        List.of("Jane Testerson"),
                        SINGLE_VALUE,
                        1
                )
        );
    }
}