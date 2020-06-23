package org.codeforamerica.shiba;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.Utils.getFormInputName;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class FormData extends HashMap<String, InputData> {
    public FormData(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }

    static FormData fillOut(PageConfiguration page, MultiValueMap<String, String> model) {
        return new FormData(
                page.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> {
                                    List<String> value = Optional.ofNullable(model)
                                            .map(modelMap -> modelMap.get(getFormInputName(input.getName())))
                                            .orElse(null);
                                    return new InputData(input.getValidationFor(model), value);
                                }
                        )));
    }

    static FormData initialize(PageConfiguration pageConfiguration, Function<DefaultValue, InputData> inputDataCreator) {
        return new FormData(
                pageConfiguration.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> Optional.ofNullable(input.getDefaultValue())
                                        .map(inputDataCreator)
                                        .orElse(new InputData())
                        )));
    }

    static Function<DefaultValue, InputData> literalInputDataCreator() {
        return defaultValue -> new InputData(List.of(defaultValue.getValue()));
    }

    static Function<DefaultValue, InputData> datasourceInputDataCreator(PageDatasource datasource, PagesData pagesData) {
        return defaultValue -> {
            FormData formData = getFormDataFrom(datasource, pagesData);

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

    static FormData getFormDataFrom(PageDatasource datasource, PagesData pagesData) {
        FormData formData = pagesData.getPage(datasource.getPageName());
        if (datasource.getInputs().isEmpty()) {
            return formData;
        }
        Map<String, InputData> inputDataMap = datasource.getInputs().stream()
                .map(inputDatasource -> Map.entry(
                        inputDatasource.getName(),
                        formData.get(inputDatasource.getName())
                                .withValueMessageKeys(inputDatasource.getValueMessageKeys())))
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new FormData(inputDataMap);
    }

    public Boolean isValid() {
        return values().stream().allMatch(InputData::getValid);
    }
}
