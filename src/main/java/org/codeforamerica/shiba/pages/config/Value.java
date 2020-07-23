package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.List;

@Data
public class Value {
    private String value;
    private List<ConditionalValue> conditionalValues = List.of();
}
