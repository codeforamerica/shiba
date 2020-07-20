package org.codeforamerica.shiba.pages;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.pages.PageUtils.getFormInputName;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class InputDataMap extends HashMap<String, InputData> {
    public InputDataMap(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }

    static InputDataMap fillOut(PageConfiguration page, MultiValueMap<String, String> model) {
        Map<String, InputData> inputDataMap = page.getFlattenedInputs().stream()
                .map(formInput -> {
                    List<String> value = Optional.ofNullable(model)
                            .map(modelMap -> modelMap.get(getFormInputName(formInput.getName())))
                            .orElse(null);
                    InputData inputData = new InputData(formInput.getValidationFor(model), value);
                    return Map.entry(formInput.getName(), inputData);
                })
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new InputDataMap(inputDataMap);
    }

    public static InputDataMap initialize(PageConfiguration pageConfiguration) {
        return new InputDataMap(
                pageConfiguration.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> Optional.ofNullable(input.getDefaultValue())
                                        .map(defaultValue -> new InputData(List.of(defaultValue)))
                                        .orElse(new InputData())
                        )));
    }

    public InputData getInputDataBy(InputDatasource inputDatasource) {
        return get(inputDatasource.getName()).withValueMessageKeys(inputDatasource.getValueMessageKeys());
    }

    public Boolean isValid() {
        return values().stream().allMatch(InputData::getValid);
    }
}
