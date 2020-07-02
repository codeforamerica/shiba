package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.List;

@Data
public class Value {
    String value;
    List<ConditionalValue> conditionalValues = List.of();
}
