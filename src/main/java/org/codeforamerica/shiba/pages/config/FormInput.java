package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class FormInput {
    private FormInputType type;
    private String name;
    private PromptMessage promptMessage;
    private String helpMessageKey;
    private String validationErrorMessageKey;
    private List<Option> options;
    private List<FormInput> followUps = Collections.emptyList();
    private String followUpsValue;
    private Validator validator;
    private Boolean readOnly = false;
    private String defaultValue;
    private Integer max;
    private Integer min;

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
