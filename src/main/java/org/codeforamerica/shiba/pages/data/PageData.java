package org.codeforamerica.shiba.pages.data;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.Validator;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.pages.PageUtils.getFormInputName;

@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class PageData extends HashMap<String, InputData> {
    public PageData(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }

    public static PageData fillOut(PageConfiguration page, MultiValueMap<String, String> model) {
        Map<String, InputData> inputDataMap = page.getFlattenedInputs().stream()
                .map(formInput -> {
                    List<String> value = Optional.ofNullable(model)
                            .map(modelMap -> modelMap.get(getFormInputName(formInput.getName())))
                            .orElse(null);
                    InputData inputData = new InputData(value, validatorsFor(formInput, model));
                    return Map.entry(formInput.getName(), inputData);
                })
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new PageData(inputDataMap);
    }

    public static PageData initialize(PageConfiguration pageConfiguration) {
        return new PageData(
                pageConfiguration.getFlattenedInputs().stream()
                        .collect(toMap(
                                FormInput::getName,
                                input -> Optional.ofNullable(input.getDefaultValue())
                                        .map(defaultValue -> new InputData(List.of(defaultValue)))
                                        .orElse(new InputData())
                        )));
    }

    public Boolean isValid() {
        return values().stream().allMatch(InputData::valid);
    }

    public Boolean satisfies(Condition condition) {
        List<String> inputValue = this.get(condition.getInput()).getValue();
        return condition.matches(inputValue);
    }

    private static List<Validator> validatorsFor(FormInput formInput, MultiValueMap<String, String> model) {
        return formInput.getValidators()
                .stream()
                .filter(validator -> validator.shouldValidate(model))
                .collect(Collectors.toList());
    }


    /**
     * Merges the InputData values of otherPage into this PageData.
     *
     * @param otherPage PageData containing values to merge.
     */
    public void mergeInputDataValues(PageData otherPage) {
        if (otherPage != null) {
            otherPage.forEach((key, value) -> {
                putIfAbsent(key, new InputData(new ArrayList<>()));
                get(key).getValue().addAll(value.getValue());
            });
        }
    }
}
