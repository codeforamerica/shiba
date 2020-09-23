package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.codeforamerica.shiba.County;

import java.util.HashMap;
import java.util.Map;

@Data
public class CountyMap<T> {
    T defaultValue;
    Map<County, T> counties = new HashMap<>();

    public T get(County county) {
        return counties.getOrDefault(county, defaultValue);
    }
}
