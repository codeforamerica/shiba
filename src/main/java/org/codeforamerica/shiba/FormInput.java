package org.codeforamerica.shiba;

import lombok.Data;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.List;

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
    String validationErrorMessageKey;
    String validationExpression = "true";
    String customFragment;

    public String getFormInputName() {
        return this.name + WEB_INPUT_ARRAY_TOKEN;
    }

    public void setAndValidate(List<String> value) {
        this.value = value;
        this.validate();
    }

    private void validate() {
        this.setValid(EXPRESSION_PARSER.parseExpression(validationExpression).getValue(this.value, Boolean.class));
    }

    public String fragment() {
        return switch (type) {
            case TEXT -> "common-input";
            case DATE -> "date-input";
            default -> customFragment;
        };
    }
}
