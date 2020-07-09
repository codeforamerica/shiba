package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationInputsProjectorTest {
    PagesConfiguration pagesConfiguration = new PagesConfiguration();
    PagesData data = new PagesData();
    InputToOutputProjectionConfiguration inputToOutputProjectionConfiguration = new InputToOutputProjectionConfiguration();
    ApplicationInputsProjector applicationInputsMapper = new ApplicationInputsProjector(pagesConfiguration, inputToOutputProjectionConfiguration);

    @Test
    void shouldCarryOverValue() {
        FormInput input1 = new FormInput();
        String input1Name = "input 1";
        input1.setName(input1Name);
        input1.setType(FormInputType.TEXT);

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input1));
        String pageName1 = "screen1";
        pagesConfiguration.getPages().put(pageName1, page);

        List<String> input1Value = List.of("input1Value");
        data.putPage(pageName1, new FormData(Map.of(
                input1Name, new InputData(Validation.NONE, input1Value)
        )));

        String groupName = "someGroup";
        ProjectionTarget projectionTarget = new ProjectionTarget();
        projectionTarget.setGroupName(groupName);
        projectionTarget.setInputs(List.of(input1Name));
        this.inputToOutputProjectionConfiguration.put(pageName1, projectionTarget);

        List<ApplicationInput> applicationInputs = applicationInputsMapper.map(data);

        assertThat(applicationInputs).contains(
                new ApplicationInput(groupName, input1Value, input1Name, ApplicationInputType.SINGLE_VALUE)
        );
    }

    @Test
    void shouldNotCarryOverValue_whenCarryOverConditionIsFalse() {
        FormInput input1 = new FormInput();
        String input1Name = "input 1";
        input1.setName(input1Name);
        input1.setType(FormInputType.TEXT);

        FormInput input2 = new FormInput();
        String input2Name = "input 2";
        input2.setName(input2Name);
        input2.setType(FormInputType.TEXT);

        PageConfiguration page1 = new PageConfiguration();
        page1.setInputs(List.of(input1, input2));
        String pageName1 = "screen1";
        pagesConfiguration.getPages().put(pageName1, page1);


        List<String> input1Value = List.of("input1Value");
        List<String> input2Value = List.of("notTheValueToTriggerTheCondition");
        data.putPage(pageName1, new FormData(Map.of(
                input1Name, new InputData(Validation.NONE, input1Value),
                input2Name, new InputData(Validation.NONE, input2Value)
        )));

        String groupName = "someGroup";
        Condition carryOverCondition = new Condition();
        carryOverCondition.setInput(input2Name);
        carryOverCondition.setValue("valueToTriggerCondition");
        ProjectionTarget projectionTarget = new ProjectionTarget();
        projectionTarget.setGroupName(groupName);
        projectionTarget.setInputs(List.of(input1Name));
        projectionTarget.setCondition(carryOverCondition);
        this.inputToOutputProjectionConfiguration.put(pageName1, projectionTarget);

        List<ApplicationInput> applicationInputs = applicationInputsMapper.map(data);

        assertThat(applicationInputs).isEmpty();
    }

    @Test
    void shouldCarryOverValue_whenCarryOverConditionIsTrue() {
        FormInput input1 = new FormInput();
        String input1Name = "input 1";
        input1.setName(input1Name);
        input1.setType(FormInputType.TEXT);

        FormInput input2 = new FormInput();
        String input2Name = "input 2";
        input2.setName(input2Name);
        input2.setType(FormInputType.TEXT);

        PageConfiguration page1 = new PageConfiguration();
        page1.setInputs(List.of(input1, input2));
        String pageName1 = "screen1";
        pagesConfiguration.getPages().put(pageName1, page1);


        List<String> input1Value = List.of("input1Value");
        String valueToTriggerCondition = "valueToTriggerCondition";
        List<String> input2Value = List.of(valueToTriggerCondition);
        data.putPage(pageName1, new FormData(Map.of(
                input1Name, new InputData(Validation.NONE, input1Value),
                input2Name, new InputData(Validation.NONE, input2Value)
        )));

        String groupName = "someGroup";
        Condition carryOverCondition = new Condition();
        carryOverCondition.setInput(input2Name);
        carryOverCondition.setValue(valueToTriggerCondition);
        ProjectionTarget projectionTarget = new ProjectionTarget();
        projectionTarget.setGroupName(groupName);
        projectionTarget.setInputs(List.of(input1Name));
        projectionTarget.setCondition(carryOverCondition);
        this.inputToOutputProjectionConfiguration.put(pageName1, projectionTarget);

        List<ApplicationInput> applicationInputs = applicationInputsMapper.map(data);

        assertThat(applicationInputs).containsOnly(
                new ApplicationInput(groupName, input1Value, input1Name, ApplicationInputType.SINGLE_VALUE)
        );
    }
}