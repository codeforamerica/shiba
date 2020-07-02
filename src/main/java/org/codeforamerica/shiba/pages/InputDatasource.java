package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.Map;

@Data
class InputDatasource {
    String name;
    Map<String, String> valueMessageKeys = Map.of();
}
