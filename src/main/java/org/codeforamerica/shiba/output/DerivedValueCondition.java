package org.codeforamerica.shiba.output;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.inputconditions.Condition;

@EqualsAndHashCode(callSuper = true)
@Data
public class DerivedValueCondition extends Condition {
    private String pageName;
}
