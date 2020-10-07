package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowInputMapper;
import org.codeforamerica.shiba.pages.config.*;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SubworkflowInputMapperTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final Subworkflows subworkflows = new Subworkflows();
    private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    SubworkflowInputMapper subworkflowInputMapper = new SubworkflowInputMapper(applicationConfiguration, Map.of());
    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    @Test
    void shouldNameSpaceValuesFromEachIteration() {
        PageWorkflowConfiguration question1Workflow = new PageWorkflowConfiguration();
        question1Workflow.setGroupName("group1");
        PageConfiguration question1Page = new PageConfiguration();
        question1Page.setName("question1");
        FormInput question1Input1 = new FormInput();
        question1Input1.setName("input1");
        question1Input1.setType(FormInputType.TEXT);
        FormInput question1Input2 = new FormInput();
        question1Input2.setName("input2");
        question1Input2.setType(FormInputType.RADIO);
        question1Page.setInputs(List.of(question1Input1, question1Input2));
        question1Workflow.setPageConfiguration(question1Page);

        PageWorkflowConfiguration question2Workflow = new PageWorkflowConfiguration();
        question2Workflow.setGroupName("group2");
        PageConfiguration question2Page = new PageConfiguration();
        question2Page.setName("question2");
        FormInput question2Input1 = new FormInput();
        question2Input1.setName("input1");
        question2Input1.setType(FormInputType.TEXT);
        question2Page.setInputs(List.of(question1Input1));
        question2Workflow.setPageConfiguration(question2Page);

        PagesData iteration1 = pagesDataBuilder.build(List.of(
                new PageDataBuilder("question1", Map.of(
                        "input1", List.of("string"),
                        "input2", List.of("coolString")
                ))
        ));

        PagesData iteration2 = pagesDataBuilder.build(List.of(
                new PageDataBuilder("question1", Map.of(
                        "input1", List.of("otherString"),
                        "input2", List.of("weirdString")
                ))
        ));

        PagesData iteration3 = pagesDataBuilder.build(List.of(
                new PageDataBuilder("question2", Map.of(
                        "input1", List.of("differentString")
                ))
        ));

        applicationConfiguration.setWorkflow(Map.of(
                "question1Workflow", question1Workflow,
                "question2Workflow", question2Workflow
        ));

        PageGroupConfiguration group2Config = new PageGroupConfiguration();
        group2Config.setStartingCount(1);
        applicationConfiguration.setPageGroups(Map.of(
                "group1", new PageGroupConfiguration(),
                "group2", group2Config
        ));

        subworkflows.addIteration("group1", iteration1);
        subworkflows.addIteration("group1", iteration2);
        subworkflows.addIteration("group2", iteration3);

        applicationData.setSubworkflows(subworkflows);
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.Other)
                .timeToComplete(null)
                .build();

        assertThat(subworkflowInputMapper.map(application, Recipient.CLIENT)).containsExactlyInAnyOrder(
                new ApplicationInput(
                        "question1",
                        "input1",
                        List.of("string"),
                        ApplicationInputType.SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "question1",
                        "input2",
                        List.of("coolString"),
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "question1",
                        "input1",
                        List.of("otherString"),
                        ApplicationInputType.SINGLE_VALUE,
                        1
                ),
                new ApplicationInput(
                        "question1",
                        "input2",
                        List.of("weirdString"),
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE,
                        1
                ),
                new ApplicationInput(
                        "question2",
                        "input1",
                        List.of("differentString"),
                        ApplicationInputType.SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "group1",
                        "count",
                        List.of("2"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "group2",
                        "count",
                        List.of("2"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldIncludeSubworkflowCountInputWhenThereAreNoIterations() {
        PageWorkflowConfiguration pageWorkflowConfig = new PageWorkflowConfiguration();
        pageWorkflowConfig.setGroupName("someGroupName");

        PageWorkflowConfiguration otherPageWorkflowConfig = new PageWorkflowConfiguration();
        otherPageWorkflowConfig.setGroupName("otherGroupName");

        PageGroupConfiguration groupConfigWithStartingCount = new PageGroupConfiguration();
        groupConfigWithStartingCount.setStartingCount(5);

        applicationConfiguration.setWorkflow(Map.of(
                "someWorkflowName", pageWorkflowConfig,
                "otherWorkflowName", otherPageWorkflowConfig
        ));

        applicationConfiguration.setPageGroups(Map.of(
                "someGroupName", new PageGroupConfiguration(),
                "otherGroupName", groupConfigWithStartingCount
        ));

        Application application = Application.builder().applicationData(new ApplicationData()).build();
        List<ApplicationInput> applicationInputs = subworkflowInputMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        "someGroupName",
                        "count",
                        List.of("0"),
                        ApplicationInputType.SINGLE_VALUE
                ),
                new ApplicationInput(
                        "otherGroupName",
                        "count",
                        List.of("5"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }
}