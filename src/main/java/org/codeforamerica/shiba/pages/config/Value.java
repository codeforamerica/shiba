package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Value {
    private String defaultValue;
    private List<ConditionalValue> conditionalValues = List.of();

    public Value(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
