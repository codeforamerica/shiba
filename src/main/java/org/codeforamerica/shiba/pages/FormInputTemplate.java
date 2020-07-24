package org.codeforamerica.shiba.pages;

import lombok.Value;
import org.codeforamerica.shiba.pages.config.*;

import java.util.Collections;
import java.util.List;

@Value
public class FormInputTemplate {
    FormInputType type;
    String name;
    PromptMessage promptMessage;
    String helpMessageKey;
    String validationErrorMessageKey;
    List<Option> options;
    List<FormInputTemplate> followUps;
    String followUpsValue;
    Boolean readOnly;
    String defaultValue;
    Integer max;
    Integer min;

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
