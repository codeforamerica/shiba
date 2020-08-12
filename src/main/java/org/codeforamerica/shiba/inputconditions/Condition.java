package org.codeforamerica.shiba.inputconditions;

import lombok.Data;

import java.util.List;

@Data
public class Condition {
    String pageName;
    String input;
    String value;
    ValueMatcher matcher = ValueMatcher.CONTAINS;

    String subworkflow;
    Integer iteration;

    public Boolean matches(List<String> inputValue) {
        return this.matcher.matches(inputValue, value);
    }
}
