package org.codeforamerica.shiba;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.Utils.getFormInputName;

@Data
public class ValidationCondition {
    String input;
    String value;
    ValidationMatcher matcher = ValidationMatcher.CONTAINS;

    public Boolean appliesTo(MultiValueMap<String, String> model) {
        List<String> inputValue = Optional.ofNullable(model)
                .map(modelMap -> modelMap.get(getFormInputName(input)))
                .orElse(List.of());
        return this.matcher.matches(inputValue, value);
    }
}
