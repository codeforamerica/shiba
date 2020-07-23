package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.config.*;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.InputDataMap;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OneToOneApplicationInputsMapperTest {
    PagesConfiguration pagesConfiguration = new PagesConfiguration();
    PagesData data = new PagesData();
    OneToOneApplicationInputsMapper oneToOneApplicationInputsMapper = new OneToOneApplicationInputsMapper(pagesConfiguration);

    @Test
    void shouldProduceAnApplicationInputForAFormInput() {
        FormInput input1 = new FormInput();
        String input1Name = "input 1";
        input1.setName(input1Name);
        input1.setType(FormInputType.TEXT);

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input1));
        String pageName = "screen1";
        page.setName(pageName);
        pagesConfiguration.setPageDefinitions(List.of(page));

        List<String> input1Value = List.of("input1Value");
        data.putPage(pageName, new InputDataMap(Map.of(input1Name, new InputData(Validation.NONE, input1Value))));

        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(data);
        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(applicationData);

        assertThat(applicationInputs).contains(
                new ApplicationInput(pageName, input1Value, input1Name, ApplicationInputType.SINGLE_VALUE)
        );
    }

    @Test
    void shouldIncludeApplicationInputsForFollowups() {
        FormInput input2 = new FormInput();
        String input2Name = "input 2";
        input2.setName(input2Name);
        input2.setType(FormInputType.TEXT);

        FormInput input3 = new FormInput();
        String input3Name = "input 3";
        input3.setName(input3Name);
        input3.setType(FormInputType.TEXT);

        input2.setFollowUps(List.of(input3));

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input2));
        String pageName = "screen1";
        page.setName(pageName);
        pagesConfiguration.setPageDefinitions(List.of(page));

        List<String> input2Value = List.of("input2Value");
        List<String> input3Value = List.of("input3Value");
        data.putPage(pageName, new InputDataMap(Map.of(
                input2Name, new InputData(Validation.NONE, input2Value),
                input3Name, new InputData(Validation.NONE, input3Value)
        )));

        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(data);
        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(applicationData);

        assertThat(applicationInputs).contains(
                new ApplicationInput(pageName, input2Value, input2Name, ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(pageName, input3Value, input3Name, ApplicationInputType.SINGLE_VALUE)
        );
    }

}