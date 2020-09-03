package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.applicationinputsmappers.OneToOneApplicationInputsMapper;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.FormInputType;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OneToOneApplicationInputsMapperTest {
    ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
    PagesData data = new PagesData();
    OneToOneApplicationInputsMapper oneToOneApplicationInputsMapper = new OneToOneApplicationInputsMapper(applicationConfiguration, Map.of());

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
        applicationConfiguration.setPageDefinitions(List.of(page));

        List<String> input1Value = List.of("input1Value");
        data.putPage(pageName, new PageData(Map.of(input1Name, InputData.builder().value(input1Value).build())));

        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(data);
        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER);
        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(pageName, input1Name, input1Value, ApplicationInputType.SINGLE_VALUE)
        );
    }

    @Test
    void shouldProduceAnApplicationInput_withMaskedValueForAFormInputWithPersonalData_whenRecipientIsClient() {
        String input1Name = "input 1";
        String maskedValue = "not-for-your-eyes";

        OneToOneApplicationInputsMapper oneToOneApplicationInputsMapper =
                new OneToOneApplicationInputsMapper(applicationConfiguration, Map.of(input1Name, maskedValue));

        FormInput input1 = new FormInput();
        input1.setName(input1Name);
        input1.setType(FormInputType.TEXT);

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input1));
        String pageName = "screen1";
        page.setName(pageName);
        applicationConfiguration.setPageDefinitions(List.of(page));

        data.putPage(pageName, new PageData(Map.of(
                input1Name, InputData.builder()
                        .value(List.of("input1Value"))
                        .build())));
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(data);

        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER);

        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs)
                .contains(new ApplicationInput(pageName, input1Name, List.of(maskedValue), ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    @ValueSource
    void shouldNotProduceAnApplicationInput_withMaskedValueForAFormInputWithPersonalData_whenRecipientIsClient_butValueIsBlank() {
        String input1Name = "input 1";
        String maskedValue = "not-for-your-eyes";

        OneToOneApplicationInputsMapper oneToOneApplicationInputsMapper =
                new OneToOneApplicationInputsMapper(applicationConfiguration, Map.of(input1Name, maskedValue));

        FormInput input1 = new FormInput();
        input1.setName(input1Name);
        input1.setType(FormInputType.TEXT);

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input1));
        String pageName = "screen1";
        page.setName(pageName);
        applicationConfiguration.setPageDefinitions(List.of(page));

        data.putPage(pageName, new PageData(Map.of(
                input1Name, InputData.builder()
                        .value(List.of(""))
                        .build())));
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(data);

        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER);

        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).isEmpty();
    }

    @Test
    void shouldProduceAnApplicationInput_withUnmaskedValueForAFormInputWithPersonalData_whenRecipientIsCaseworker() {
        String input1Name = "input 1";

        OneToOneApplicationInputsMapper oneToOneApplicationInputsMapper =
                new OneToOneApplicationInputsMapper(applicationConfiguration, Map.of(input1Name, "not-for-your-eyes"));

        FormInput input1 = new FormInput();
        input1.setName(input1Name);
        input1.setType(FormInputType.TEXT);

        PageConfiguration page = new PageConfiguration();
        page.setInputs(List.of(input1));
        String pageName = "screen1";
        page.setName(pageName);
        applicationConfiguration.setPageDefinitions(List.of(page));

        List<String> input1Value = List.of("input1Value");
        data.putPage(pageName, new PageData(Map.of(
                input1Name, InputData.builder()
                        .value(input1Value)
                        .build())));
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(data);

        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER);

        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(application, Recipient.CASEWORKER);

        assertThat(applicationInputs).contains(
                new ApplicationInput(pageName, input1Name, input1Value, ApplicationInputType.SINGLE_VALUE)
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
        applicationConfiguration.setPageDefinitions(List.of(page));

        List<String> input2Value = List.of("input2Value");
        List<String> input3Value = List.of("input3Value");
        data.putPage(pageName, new PageData(Map.of(
                input2Name, InputData.builder().value(input2Value).build(),
                input3Name, InputData.builder().value(input3Value).build()
        )));

        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(data);
        Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER);
        List<ApplicationInput> applicationInputs = oneToOneApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(applicationInputs).contains(
                new ApplicationInput(pageName, input2Name, input2Value, ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(pageName, input3Name, input3Value, ApplicationInputType.SINGLE_VALUE)
        );
    }

}