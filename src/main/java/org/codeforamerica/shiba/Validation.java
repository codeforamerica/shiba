package org.codeforamerica.shiba;


import java.util.List;
import java.util.function.Predicate;

public enum Validation {
    NONE(strings -> true, null),
    NOT_BLANK(strings -> !String.join("", strings).isBlank(), "general.validation.make-sure-you-answer-this-question"),
    SSN(strings -> List.of(0, 9).contains(String.join("", strings).length()), "general.validation.make-sure-you-answer-this-question");

    private final Predicate<List<String>> rule;
    private final String errorMessageKey;

    Validation(Predicate<List<String>> rule, String errorMessageKey) {
        this.rule = rule;
        this.errorMessageKey = errorMessageKey;
    }

    public Boolean apply(List<String> value) {
        return this.rule.test(value);
    }

    public String getErrorMessageKey() {
        return errorMessageKey;
    }
}
