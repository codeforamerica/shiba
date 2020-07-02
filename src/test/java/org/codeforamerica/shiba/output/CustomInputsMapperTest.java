package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.pages.FormData;
import org.codeforamerica.shiba.pages.InputData;
import org.codeforamerica.shiba.pages.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomInputsMapperTest {
    @Test
    void shouldProduceSingleValueInputsForTheInputsOnTheConfiguredPages() {
        PagesData data = new PagesData();
        String pageName = "somePage";
        String inputName = "someInput";
        List<String> inputValue = List.of("someValue");
        data.putPage(pageName, new FormData(Map.of(inputName, new InputData(inputValue))));

        CustomInputsMapper customInputsMapper = new CustomInputsMapper(List.of(pageName));
        List<ApplicationInput> applicationInputs = customInputsMapper.map(data);

        assertThat(applicationInputs).contains(
                new ApplicationInput(
                        pageName,
                        inputValue,
                        inputName,
                        ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProduceNoInputsWhenNoPagesAreConfigured() {
        PagesData data = new PagesData();
        String pageName = "somePage";
        String inputName = "someInput";
        List<String> inputValue = List.of("someValue");
        data.putPage(pageName, new FormData(Map.of(inputName, new InputData(inputValue))));

        CustomInputsMapper customInputsMapper = new CustomInputsMapper(List.of());
        List<ApplicationInput> applicationInputs = customInputsMapper.map(data);

        assertThat(applicationInputs).isEmpty();
    }
}