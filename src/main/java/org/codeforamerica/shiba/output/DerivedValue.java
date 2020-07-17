package org.codeforamerica.shiba.output;

import lombok.Data;

@Data
public class DerivedValue {
    private String value;
    private ApplicationInputType type;
    private CompositeCondition condition;
}
