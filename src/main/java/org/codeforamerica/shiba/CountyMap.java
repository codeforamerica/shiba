package org.codeforamerica.shiba;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CountyMap<T> {
    T defaultValue;
    Map<County, T> counties = new HashMap<>();

    public T get(County county) { return counties.getOrDefault(county, defaultValue); }
}
