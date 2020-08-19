package org.codeforamerica.shiba.inputconditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
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

    public boolean appliesForAllIterations() {
        return getSubworkflow() != null && getIteration() == null;
    }
}
