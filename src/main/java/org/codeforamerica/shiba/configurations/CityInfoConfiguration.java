package org.codeforamerica.shiba.configurations;

import lombok.Data;

import java.util.Map;

@Data
public class CityInfoConfiguration {
    public Map<String, Map<String, String>> cityToZipAndCountyMapping;
}
