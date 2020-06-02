package org.codeforamerica.shiba;

import lombok.Data;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.List;
import java.util.Objects;

@Data
public class FormInput {
    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    FormInputType type;
    String name;
    String promptMessageKey;
    String helpMessageKey;
    Boolean valid = true;
    List<String> value;
    Validation validation = Validation.NONE;
    String validationErrorMessageKey;
    List<Option> options;
    FormInputWithFollowUps inputWithFollowUps;

    public String getFormInputName() {
        return this.name + WEB_INPUT_ARRAY_TOKEN;
    }

    public List<String> getNonNullValue() {
        return Objects.requireNonNullElseGet(value, List::of);
    }

    public void setAndValidate(List<String> value) {
        this.value = value;
        this.validate();
    }

    private void validate() {
        this.setValid(this.validation.apply(this.value));
        this.validationErrorMessageKey = this.validation.getErrorMessageKey();
    }

    public String fragment() {
        return switch (type) {
            case INPUT_WITH_FOLLOW_UP -> "input-with-follow-up";
            case TEXT, NUMBER -> "common-input";
            case DATE -> "date-input";
            case RADIO -> "radio-input";
        };
    }
}
