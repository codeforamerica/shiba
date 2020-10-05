package org.codeforamerica.shiba.application.parsers;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ParsingCoordinates {
    Map<String, PageInputCoordinates> pageInputs = new HashMap<>();
    String groupName;
}
