package org.codeforamerica.shiba;

import lombok.Data;

@Data
public class ConditionalValue {
    Condition condition;
    String value;
}
