package org.codeforamerica.shiba;

import lombok.Data;

import java.util.Map;

@Data
class InputDatasource {
    String name;
    Map<String, String> valueMessageKeys = Map.of();
}
