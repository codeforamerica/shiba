package org.codeforamerica.shiba;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

@Data
public class Validator {
    Validation validation = Validation.NONE;
    ValidationCondition condition;

    Validation validationFor(MultiValueMap<String, String> model) {
        boolean applyValidation = Optional.ofNullable(this.condition)
                .map(nonNullCondition -> nonNullCondition.appliesTo(model))
                .orElse(true);
        return applyValidation ? validation : Validation.NONE;
    }
}
