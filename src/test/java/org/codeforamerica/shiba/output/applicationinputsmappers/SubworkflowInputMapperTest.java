package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.inputconditions.ValueMatcher;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.FormInputType;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.codeforamerica.shiba.pages.config.PageWorkflowConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;

class SubworkflowInputMapperTest {

  private final ApplicationData applicationData = new ApplicationData();
  private final Subworkflows subworkflows = new Subworkflows();
  private final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
  SubworkflowInputMapper subworkflowInputMapper = new SubworkflowInputMapper(
      applicationConfiguration, Map.of());

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

    PagesData iteration1 = new PagesDataBuilder()
        .withPageData("question1", Map.of(
            "input1", "string",
            "input2", "coolString"
        )).build();

    PagesData iteration2 = new PagesDataBuilder()
        .withPageData("question1", Map.of(
            "input1", "otherString",
            "input2", "weirdString"
        )).build();

    PagesData iteration3 = new PagesDataBuilder()
        .withPageData("question2",
            "input1", "differentString"
        ).build();

    applicationConfiguration.setPageDefinitions(List.of(
        question1Page,
        question2Page
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

    assertThat(subworkflowInputMapper
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker())).contains(
        new DocumentField(
            "question1",
            "input1",
            List.of("string"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "question1",
            "input2",
            List.of("coolString"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "question1",
            "input1",
            List.of("otherString"),
            DocumentFieldType.SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "question1",
            "input2",
            List.of("weirdString"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE,
            1
        ),
        new DocumentField(
            "question2",
            "input1",
            List.of("differentString"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            "group1",
            "count",
            List.of("2"),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "group2",
            "count",
            List.of("2"),
            DocumentFieldType.SINGLE_VALUE
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
    List<DocumentField> documentFields = subworkflowInputMapper
        .prepareDocumentFields(application, null, Recipient.CLIENT, null);

    assertThat(documentFields).contains(
        new DocumentField(
            "someGroupName",
            "count",
            List.of("0"),
            DocumentFieldType.SINGLE_VALUE
        ),
        new DocumentField(
            "otherGroupName",
            "count",
            List.of("5"),
            DocumentFieldType.SINGLE_VALUE
        )
    );
  }

  @Test
  void shouldIncludeFilteredInputs() {
    Condition condition1 = new Condition("question1", "input1", "true", ValueMatcher.CONTAINS, null,
        null);
    String prefix1 = "prefix1";
    Condition condition2 = new Condition("question1", "input1", "false", ValueMatcher.CONTAINS,
        null, null);
    String prefix2 = "prefix2";
    Map<String, Condition> addedScope = Map.of(prefix1, condition1, prefix2, condition2);
    PageGroupConfiguration group1 = new PageGroupConfiguration();
    group1.setAddedScope(addedScope);

    PageWorkflowConfiguration question1Workflow = new PageWorkflowConfiguration();
    question1Workflow.setGroupName("group1");
    PageConfiguration question1Page = new PageConfiguration();
    question1Workflow.setPageConfiguration(question1Page);

    question1Page.setName("question1");
    FormInput question1Input1 = new FormInput();
    question1Input1.setName("input1");
    question1Input1.setType(FormInputType.TEXT);

    FormInput question1Input2 = new FormInput();
    question1Input2.setName("input2");
    question1Input2.setType(FormInputType.RADIO);
    question1Page.setInputs(List.of(question1Input1, question1Input2));

    PageWorkflowConfiguration question2Workflow = new PageWorkflowConfiguration();
    question2Workflow.setGroupName("group1");
    PageConfiguration question2Page = new PageConfiguration();
    question2Workflow.setPageConfiguration(question2Page);

    question2Page.setName("question2");
    FormInput question2Input1 = new FormInput();
    question2Input1.setName("input1");
    question2Input1.setType(FormInputType.TEXT);

    question2Page.setInputs(List.of(question2Input1));

    PagesData iteration1 = new PagesDataBuilder()
        .withPageData("question1", Map.of(
            "input1", "false",
            "input2", "coolString"
        ))
        .withPageData("question2", Map.of(
            "input1", "foo"
        )).build();

    PagesData iteration2 = new PagesDataBuilder()
        .withPageData("question1", Map.of(
            "input1", "true",
            "input2", "weirdString"
        ))
        .withPageData("question2", Map.of(
            "input1", "bar"
        )).build();

    applicationConfiguration.setPageDefinitions(List.of(
        question1Page,
        question2Page
    ));

    applicationConfiguration.setWorkflow(Map.of(
        "question1Workflow", question1Workflow,
        "question2Workflow", question2Workflow
    ));

    applicationConfiguration.setPageGroups(Map.of(
        "group1", group1
    ));

    subworkflows.addIteration("group1", iteration1);
    subworkflows.addIteration("group1", iteration2);

    applicationData.setSubworkflows(subworkflows);
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(County.Other)
        .timeToComplete(null)
        .build();

    List<DocumentField> mapResult = subworkflowInputMapper
        .prepareDocumentFields(application, null, Recipient.CLIENT,
            new SubworkflowIterationScopeTracker());
    assertThat(mapResult).contains(
        new DocumentField(
            prefix1 + "_question1",
            "input1",
            List.of("true"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            prefix1 + "_question1",
            "input2",
            List.of("weirdString"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE,
            0
        ),
        new DocumentField(
            prefix1 + "_question2",
            "input1",
            List.of("bar"),
            DocumentFieldType.SINGLE_VALUE,
            0
        )
    );
    assertThat(mapResult).contains(
        new DocumentField(
            prefix2 + "_question1",
            "input1",
            List.of("false"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ),
        new DocumentField(
            prefix2 + "_question1",
            "input2",
            List.of("coolString"),
            DocumentFieldType.ENUMERATED_SINGLE_VALUE,
            0
        ),
        new DocumentField(
            prefix2 + "_question2",
            "input1",
            List.of("foo"),
            DocumentFieldType.SINGLE_VALUE,
            0
        )
    );
  }
}
