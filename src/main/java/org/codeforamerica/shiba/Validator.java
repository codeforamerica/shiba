package org.codeforamerica.shiba;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.Utils.getFormInputName;

@Data
public class Validator {
    Validation validation = Validation.NONE;
    ValidationCondition condition;

    Validation validationFor(MultiValueMap<String, String> model) {
        boolean applyValidation = Optional.ofNullable(this.condition)
                .map(nonNullCondition -> Optional.ofNullable(model)
                        .map(modelMap -> modelMap.get(getFormInputName(nonNullCondition.input)))
                        .orElse(List.of())
                        .contains(nonNullCondition.value))
                .orElse(true);
        return applyValidation ? validation : Validation.NONE;
    }
}
