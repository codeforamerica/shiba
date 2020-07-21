package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;

@Data
public class FormInput {
    FormInputType type;
    String name;
    PromptMessage promptMessage;
    String helpMessageKey;
    String validationErrorMessageKey;
    List<Option> options;
    List<FormInput> followUps = Collections.emptyList();
    String followUpsValue;
    Validator validator = new Validator();
    Boolean readOnly = false;
    String defaultValue;
    Integer max;
    Integer min;

    public Validation getValidationFor(MultiValueMap<String, String> model) {
        return this.validator.validationFor(model);
    }

    public String fragment() {
        return switch (type) {
            case TEXT, NUMBER, SELECT, MONEY -> "single-input";
            case DATE -> "date-input";
            case RADIO -> "radio-input";
            case CHECKBOX -> "checkbox-input";
            case YES_NO -> "yes-no-input";
            case INCREMENTER -> "incrementer-input";
        };
    }

    @SuppressWarnings("unused")
    public boolean hasFollowUps() {
        return !this.followUps.isEmpty();
    }
}
