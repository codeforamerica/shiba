package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.pages.PageUtils.getFormInputName;

@Data
public class Condition {
    String input;
    String value;
    ValueMatcher matcher = ValueMatcher.CONTAINS;

    public Boolean appliesTo(MultiValueMap<String, String> model) {
        List<String> inputValue = Optional.ofNullable(model)
                .map(modelMap -> modelMap.get(getFormInputName(input)))
                .orElse(List.of());
        return this.matcher.matches(inputValue, value);
    }

    public Boolean appliesTo(InputDataMap inputDataMap) {
        List<String> inputValue = Optional.ofNullable(inputDataMap)
                .map(nonNullInputDataMap -> nonNullInputDataMap.get(input).getValue())
                .orElse(List.of());
        return this.matcher.matches(inputValue, value);
    }

}
