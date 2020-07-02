package org.codeforamerica.shiba.pages;

import lombok.Data;

@Data
public class ConditionalValue {
    Condition condition;
    String value;
}
