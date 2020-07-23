package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.Map;

@Data
public class InputDatasource {
    private String name;
    private Map<String, String> valueMessageKeys = Map.of();
}
