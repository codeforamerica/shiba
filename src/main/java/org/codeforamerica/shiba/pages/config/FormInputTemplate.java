package org.codeforamerica.shiba.pages.config;

import lombok.Value;

import java.util.List;

@Value
public class FormInputTemplate {
    FormInputType type;
    String name;
    String customInputFragment;
    PromptMessage promptMessage;
    String helpMessageKey;
    String placeholder;
    String validationErrorMessageKey;
    OptionsWithDataSourceTemplate options;
    List<FormInputTemplate> followUps;
    List<String> followUpValues;
    Boolean readOnly;
    String defaultValue;
    Integer max;
    Integer min;

    public String fragment() {
        return switch (type) {
            case TEXT, LONG_TEXT, NUMBER, SELECT, MONEY, TEXTAREA, HOURLY_WAGE, PHONE, SSN -> "single-input";
            case DATE -> "date-input";
            case RADIO -> "radio-input";
            case CHECKBOX -> "checkbox-input";
            case PEOPLE_CHECKBOX -> "people-checkbox-input";
            case YES_NO -> "yes-no-input";
            case HIDDEN -> "hidden-input";
            case CUSTOM -> customInputFragment;
        };
    }

    @SuppressWarnings("unused")
    public boolean hasFollowUps() {
        return !this.followUps.isEmpty();
    }
}
