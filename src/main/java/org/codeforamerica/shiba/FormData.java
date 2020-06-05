package org.codeforamerica.shiba;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

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
                                input -> new InputData(input.getValidation(), model.get(input.getFormInputName()))
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
