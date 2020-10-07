package org.codeforamerica.shiba.inputconditions;

import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public enum ValueMatcher {
    EMPTY((testValue, ignoredTargetValue) -> testValue.stream().allMatch(String::isEmpty)),
    NONE_SELECTED((testValue, ignoredTargetValue) -> testValue.isEmpty()),
    CONTAINS(List::contains),
    NOT_EMPTY((testValue, ignoredTargetValue) -> !String.join("", testValue).isBlank()),
    DOES_NOT_CONTAIN((testValue, targetValue) -> !testValue.contains(targetValue));

    private final BiFunction<List<String>, String, Boolean> matcher;

    ValueMatcher(BiFunction<List<String>, String, Boolean> matcher) {
        this.matcher = matcher;
    }

    Boolean matches(List<String> testValue, String targetValue) {
        return this.matcher.apply(testValue, targetValue);
    }
}
