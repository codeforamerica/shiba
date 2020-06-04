package org.codeforamerica.shiba;

import lombok.Value;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

@Value
public class InputData {
    Boolean valid;
    List<String> value;

    InputData(Validation validation, List<String> value) {
        this.valid = validation.apply(value);
        this.value = value;
    }

    public InputData() {
        this.valid = true;
        this.value = emptyList();
    }

    @SuppressWarnings("unused")
    public List<String> getNonNullValue() {
        return Objects.requireNonNullElseGet(value, List::of);
    }

}
