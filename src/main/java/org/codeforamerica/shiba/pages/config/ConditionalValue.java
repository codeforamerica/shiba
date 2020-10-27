package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.output.CompositeCondition;

@Data
public class ConditionalValue {
    private CompositeCondition compositeCondition;
    private Condition condition;
    private String value;

    public boolean isCompositeCondition(CompositeCondition compositeCondition) {
        return compositeCondition != null;
    }
}
