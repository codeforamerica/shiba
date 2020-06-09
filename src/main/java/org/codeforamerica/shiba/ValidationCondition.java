package org.codeforamerica.shiba;

import lombok.Data;

@Data
public class ValidationCondition {
    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";

    String input;
    String value;

    public String getFormInputName() {
        return this.input + WEB_INPUT_ARRAY_TOKEN;
    }
}
