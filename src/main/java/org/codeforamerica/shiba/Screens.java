package org.codeforamerica.shiba;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class Screens extends HashMap<String, Form> {
    public Map<String, List<FormInput>> unwrapFormWithFlattenedInputs() {
        return this.entrySet().stream()
                .collect(toMap(
                        Entry::getKey,
                        entry -> entry.getValue().getFlattenedInputs()));
    }
}
