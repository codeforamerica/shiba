package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

@Data
public class ConditionalValue {
    private Condition condition;
    private String value;
}
