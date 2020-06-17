package org.codeforamerica.shiba;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.Utils.getFormInputName;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class FormData extends HashMap<String, InputData> {
    public FormData(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }

    static FormData create(Page page, MultiValueMap<String, String> model) {
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

    static FormData create(Page page) {
        return new FormData(
                page.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> Optional.ofNullable(input.getDefaultValue())
                                        .map(defaultValue -> new InputData(List.of(defaultValue.getValue())))
                                        .orElse(new InputData())
                        )));
    }

    public static FormData create(Page page, PageDatasource datasource, Map<String, FormData> data) {
        Map<String, InputData> inputDataMap = page.getFlattenedInputs().stream()
                .collect(toMap(
                        FormInput::getName,
                        input -> Optional.ofNullable(input.getDefaultValue())
                                .map(defaultValue -> switch (defaultValue.getType()) {
                                    case LITERAL -> new InputData(List.of(defaultValue.getValue()));
                                    case DATASOURCE_REFERENCE -> {
                                        FormData formData = data.get(datasource.getScreenName());
                                        yield defaultValue.conditionAppliesTo(formData) ?
                                                new InputData(formData.get(defaultValue.getValue()).getValue()) :
                                                new InputData();
                                    }
                                })
                                .orElse(new InputData())
                ));
        return new FormData(inputDataMap);
    }

    public static FormData create(PageDatasource datasource, Map<String, FormData> data) {
        FormData formData = data.get(datasource.getScreenName());
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

    Boolean isValid() {
        return values().stream().allMatch(InputData::getValid);
    }
}
