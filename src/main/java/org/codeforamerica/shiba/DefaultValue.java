package org.codeforamerica.shiba;

import lombok.Data;

@Data
public class DefaultValue {
    DefaultValueType type = DefaultValueType.LITERAL;
    String value;
    Condition condition;

    public boolean conditionAppliesTo(FormData formData) {
        return this.condition == null || this.condition.appliesTo(formData);
    }
}