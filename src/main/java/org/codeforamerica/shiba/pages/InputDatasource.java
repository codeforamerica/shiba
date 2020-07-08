package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.Map;

@Data
class InputDatasource {
    private String name;
    private Map<String, String> valueMessageKeys = Map.of();
    private String defaultValue;
}
