package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.pages.PageUtils.getFormInputName;

@Data
public class Validator {
    private Validation validation = Validation.NONE;
    private String errorMessageKey;
    private Condition condition;

    public Boolean shouldValidate(MultiValueMap<String, String> model) {
        if (condition == null) {
            return true;
        }

        List<String> inputValue = Optional.ofNullable(model)
                .map(modelMap -> modelMap.get(getFormInputName(condition.getInput())))
                .orElse(List.of());
        return condition.matches(inputValue);
    }
}
