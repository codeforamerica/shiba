package org.codeforamerica.shiba.pages.data;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.Validation;
import org.codeforamerica.shiba.pages.config.Validator;
import org.jetbrains.annotations.NotNull;
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
                    InputData inputData = new InputData(validationFor(formInput, model), value);
                    return Map.entry(formInput.getName(), inputData);
                })
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new PageData(inputDataMap);
    }

    static PageData initialize(PageConfiguration pageConfiguration) {
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
        return values().stream().allMatch(InputData::getValid);
    }

    public Boolean satisfies(Condition condition) {
        List<String> inputValue = this.get(condition.getInput()).getValue();
        return condition.matches(inputValue);
    }

    @NotNull
    private static Validation validationFor(FormInput formInput, MultiValueMap<String, String> model) {
        return Optional.ofNullable(formInput.getValidator())
                .filter(validator -> Optional.ofNullable(validator.getCondition())
                        .map(condition -> {
                            List<String> inputValue = Optional.ofNullable(model)
                                    .map(modelMap -> modelMap.get(getFormInputName(condition.getInput())))
                                    .orElse(List.of());
                            return condition.matches(inputValue);
                        })
                        .orElse(true))
                .map(Validator::getValidation)
                .orElse(Validation.NONE);
    }
}
