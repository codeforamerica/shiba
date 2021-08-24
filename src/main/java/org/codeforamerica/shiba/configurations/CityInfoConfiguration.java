package org.codeforamerica.shiba.configurations;

import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Data
public class CityInfoConfiguration {

  public Map<String, Map<String, String>> cityToZipAndCountyMapping = new TreeMap<>();
}
