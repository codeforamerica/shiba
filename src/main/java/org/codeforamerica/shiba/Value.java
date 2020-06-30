package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;

@Data
public class Value {
    String value;
    List<ConditionalValue> conditionalValues = List.of();
}
