package org.codeforamerica.shiba.configurations;

import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

@Data
public class CityInfoConfiguration {
    public Map<String, Map<String, String>> cityToZipAndCountyMapping = new TreeMap<>();
}
