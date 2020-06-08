package org.codeforamerica.shiba;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

@Value
public class InputData {
    Boolean valid;
    @NotNull List<String> value;

    InputData(Validation validation, List<String> value) {
        this.value = Objects.requireNonNullElseGet(value, List::of);
        this.valid = validation.apply(this.value);
    }

    public InputData() {
        this.valid = true;
        this.value = emptyList();
    }
}
