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

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class FormData extends HashMap<String, InputData> {
    public FormData(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }

    static FormData create(Form form, MultiValueMap<String, String> model) {
        return new FormData(
                form.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> {
                                    Validator validator = input.getValidator();
                                    Boolean applyValidation = Optional.ofNullable(validator.getCondition())
                                            .map(condition -> Optional.ofNullable(model)
                                                    .map(modelMap -> modelMap.get(condition.getFormInputName()))
                                                    .orElse(List.of())
                                                    .contains(condition.value))
                                            .orElse(true);
                                    List<String> value = Optional.ofNullable(model)
                                            .map(modelMap -> modelMap.get(input.getFormInputName()))
                                            .orElse(null);
                                    return new InputData(validator.getValidation(), value, applyValidation);
                                }
                        )));
    }

    static FormData create(Form form) {
        return new FormData(
                form.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> new InputData()
                        )));
    }

    Boolean isValid() {
        return values().stream().allMatch(InputData::getValid);
    }
}
