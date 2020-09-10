package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.config.*;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SubworkflowInputMapperTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final Subworkflows subworkflows = new Subworkflows();
    private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    SubworkflowInputMapper subworkflowInputMapper = new SubworkflowInputMapper(applicationConfiguration);

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

        applicationConfiguration.setWorkflow(Map.of(
                "question1Workflow", question1Workflow,
                "question2Workflow", question2Workflow
                ));

        PagesData iteration1 = new PagesData(
                Map.of(
                        "question1",
                        new PageData(Map.of(
                                "input1", InputData.builder().value(List.of("string")).build(),
                                "input2", InputData.builder().value(List.of("coolString")).build())
                        ))
        );

        PagesData iteration2 = new PagesData(
                Map.of(
                        "question1",
                        new PageData(Map.of(
                                "input1", InputData.builder().value(List.of("otherString")).build(),
                                "input2", InputData.builder().value(List.of("weirdString")).build())
                        ))
        );

        PagesData iteration3 = new PagesData(
                Map.of(
                        "question2",
                        new PageData(Map.of("input1", InputData.builder().value(List.of("differentString")).build()))
                )
        );

        subworkflows.addIteration("group1", iteration1);
        subworkflows.addIteration("group1", iteration2);
        subworkflows.addIteration("group2", iteration3);

        applicationData.setSubworkflows(subworkflows);
        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER, "");

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
                        List.of("1"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }
}