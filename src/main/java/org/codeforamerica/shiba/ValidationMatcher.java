package org.codeforamerica.shiba;

import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public enum ValidationMatcher {
    NOT_PRESENT((testValue, ignoredTargetValue) -> testValue.isEmpty()),
    CONTAINS(List::contains);

    private final BiFunction<List<String>, String, Boolean> matcher;

    ValidationMatcher(BiFunction<List<String>, String, Boolean> matcher) {
        this.matcher = matcher;
    }

    public Boolean matches(List<String> testValue, String targetValue) {
        return this.matcher.apply(testValue, targetValue);
    }
}
