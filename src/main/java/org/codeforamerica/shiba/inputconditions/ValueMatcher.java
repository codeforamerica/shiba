package org.codeforamerica.shiba.inputconditions;

import java.util.List;
import java.util.function.BiFunction;

/* Matcher used for Condition */
public enum ValueMatcher {
    EMPTY((testValue, ignoredTargetValue) -> testValue.stream().allMatch(String::isEmpty)),
    NONE_SELECTED((testValue, ignoredTargetValue) -> testValue.isEmpty()),
    CONTAINS(List::contains),
    NOT_EMPTY((testValue, ignoredTargetValue) -> !String.join("", testValue).isBlank()),
    CONTAINS_STRING_OTHER_THAN((testValue, targetValue) -> testValue.stream().anyMatch(string -> !string.equals(targetValue))),
    DOES_NOT_CONTAIN((testValue, targetValue) -> !testValue.contains(targetValue)),
    CONTAINS_SUBSTRING((testValue, targetValue) -> testValue.stream().anyMatch(string -> string.contains(targetValue))),
    DOES_NOT_CONTAIN_SUBSTRING((testValue, targetValue) -> testValue.stream().noneMatch(string -> string.contains(targetValue)));

    private final BiFunction<List<String>, String, Boolean> matcher;

    ValueMatcher(BiFunction<List<String>, String, Boolean> matcher) {
        this.matcher = matcher;
    }

    public Boolean matches(List<String> testValue, String targetValue) {
        return this.matcher.apply(testValue, targetValue);
    }
}
