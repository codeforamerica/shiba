package org.codeforamerica.shiba;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class FormInput {
    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";

    FormInputType type;
    String name;
    String promptMessageKey;
    String helpMessageKey;
    String validationErrorMessageKey;
    List<Option> options;
    List<FormInput> followUps = Collections.emptyList();
    String followUpsValue;
    Validator validator = new Validator();

    public String getFormInputName() {
        return this.name + WEB_INPUT_ARRAY_TOKEN;
    }

    public String fragment() {
        return switch (type) {
            case TEXT, NUMBER, SELECT -> "single-input";
            case DATE -> "date-input";
            case RADIO -> "radio-input";
            case CHECKBOX -> "checkbox-input";
        };
    }

    @SuppressWarnings("unused")
    public boolean hasFollowUps() {
        return !this.followUps.isEmpty();
    }
}
