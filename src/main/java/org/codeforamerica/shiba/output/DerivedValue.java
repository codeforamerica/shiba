package org.codeforamerica.shiba.output;

import lombok.Data;

@Data
public class DerivedValue {
    private DerivedValueConfiguration value;
    private ApplicationInputType type;
    private CompositeCondition condition;
}
