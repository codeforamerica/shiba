package org.codeforamerica.shiba.pages.data;

import lombok.Builder;
import lombok.Value;
import org.codeforamerica.shiba.pages.config.Validation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

@Value
@Builder
public class InputData {
    @NotNull Boolean valid;
    @NotNull List<String> value;

    InputData(Validation validation,
              List<String> value) {
        List<String> valueWithDefault = Objects.requireNonNullElseGet(value, List::of);
        this.value = valueWithDefault;
        this.valid = validation.apply(valueWithDefault);
    }

    InputData() {
        this(true, emptyList());
    }

    InputData(@NotNull List<String> value) {
        this(true, value);
    }

    private InputData(@NotNull Boolean valid,
                      @NotNull List<String> value) {
        this.valid = valid;
        this.value = value;
    }
}
