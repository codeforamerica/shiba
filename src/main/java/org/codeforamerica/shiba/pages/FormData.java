package org.codeforamerica.shiba.pages;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.pages.PageUtils.getFormInputName;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class FormData extends HashMap<String, InputData> {
    public FormData(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }

    static FormData fillOut(PageConfiguration page, MultiValueMap<String, String> model) {
        Stream<Entry<String, InputData>> formInputEntries = page.getFlattenedInputs().stream()
                .map(formInput -> {
                    List<String> value = Optional.ofNullable(model)
                            .map(modelMap -> modelMap.get(getFormInputName(formInput.getName())))
                            .orElse(null);
                    InputData inputData = new InputData(formInput.getValidationFor(model), value);
                    return Map.entry(formInput.getName(), inputData);
                });
        Stream<Entry<String, InputData>> additionalDataEntries = page.getAdditionalData().stream()
                .map(additionalDatum -> {
                    InputData inputData = new InputData(List.of(page.resolve(model, additionalDatum.getValue())));
                    return Map.entry(additionalDatum.getName(), inputData);
                });
        Map<String, InputData> allData = Stream.concat(formInputEntries, additionalDataEntries)
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new FormData(allData);
    }

    public static FormData initialize(PageConfiguration pageConfiguration, Function<DefaultValue, InputData> inputDataCreator) {
        return new FormData(
                pageConfiguration.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> Optional.ofNullable(input.getDefaultValue())
                                        .map(inputDataCreator)
                                        .orElse(new InputData())
                        )));
    }

    public static Function<DefaultValue, InputData> literalInputDataCreator() {
        return defaultValue -> new InputData(List.of(defaultValue.getValue()));
    }

    static Function<DefaultValue, InputData> datasourceInputDataCreator(List<PageDatasource> datasources, PagesData pagesData) {
        return defaultValue -> {
            FormData formData = getFormDataFrom(datasources, pagesData);

            List<String> inputValue = switch (defaultValue.getType()) {
                case LITERAL -> List.of(defaultValue.getValue());
                case DATASOURCE_REFERENCE -> Optional.ofNullable(formData.get(defaultValue.getValue()))
                        .map(InputData::getValue)
                        .orElseThrow(() -> new RuntimeException(
                                String.format("Configuration mismatch! The desired datasource reference '%s' " +
                                        "is not accessible.", defaultValue.getValue())));
            };

            return defaultValue.conditionAppliesTo(formData) ?
                    new InputData(inputValue) :
                    new InputData();
        };
    }

    static FormData getFormDataFrom(List<PageDatasource> datasources, PagesData pagesData) {
        Map<String, InputData> formDatas = datasources.stream()
                .map(datasource -> getFormDataFrom(datasource, pagesData))
                .flatMap(formData -> formData.entrySet().stream())
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new FormData(formDatas);
    }

    static FormData getFormDataFrom(PageDatasource datasource, PagesData pagesData) {
        FormData formData = pagesData.getPage(datasource.getPageName());
        if (datasource.getInputs().isEmpty()) {
            return Optional.ofNullable(formData)
                    .orElseThrow(() -> new RuntimeException(String.format("Datasource %s is skipped and no default is provided.", datasource.getPageName())));
        }
        Map<String, InputData> inputDataMap = datasource.getInputs().stream()
                .map(inputDatasource -> {
                    InputData inputData = Optional.ofNullable(formData)
                            .map(data -> data.get(inputDatasource.getName())
                                    .withValueMessageKeys(inputDatasource.getValueMessageKeys()))
                            .orElseGet(() -> new InputData(List.of(inputDatasource.getDefaultValue())));
                    return Map.entry(
                            inputDatasource.getName(),
                            inputData);
                })
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new FormData(inputDataMap);
    }

    public Boolean isValid() {
        return values().stream().allMatch(InputData::getValid);
    }
}
