package org.codeforamerica.shiba.output.applicationinputsmappers;

import lombok.Data;

import java.util.Map;

@Data
public class OutputMapping {
    private String groupName;
    private String fieldName;
    private Map<String, String> mappings;
}
