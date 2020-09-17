package org.codeforamerica.shiba.output.caf;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ParsingCoordinates {
    Map<String, PageInputCoordinates> pageInputs = new HashMap<>();
    String groupName;
}
