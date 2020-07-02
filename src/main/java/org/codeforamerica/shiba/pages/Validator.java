package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

@Data
public class Validator {
    Validation validation = Validation.NONE;
    Condition condition;

    Validation validationFor(MultiValueMap<String, String> model) {
        boolean applyValidation = Optional.ofNullable(this.condition)
                .map(condition -> condition.appliesTo(model))
                .orElse(true);
        return applyValidation ? validation : Validation.NONE;
    }
}
