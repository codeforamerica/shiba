package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.List;

@Data
public class Value {
    private String value;
    private List<ConditionalValue> conditionalValues = List.of();

}
