package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.List;
import java.util.function.Function;

@Data
public class Value {
    private String value;
    private List<ConditionalValue> conditionalValues = List.of();

    public String resolve(Function<Condition, Boolean> conditionFunction) {
        return this.getConditionalValues().stream()
                .filter(conditionalValue -> {
                    Condition condition = conditionalValue.getCondition();
                    return conditionFunction.apply(condition);
                })
                .findFirst()
                .map(ConditionalValue::getValue)
                .orElse(this.getValue());
    }

}
