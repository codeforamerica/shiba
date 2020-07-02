package org.codeforamerica.shiba.pages;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Value
@With
public class InputData {
    @NotNull Boolean valid;
    @NotNull List<String> value;
    @NotNull Map<String, String> valueMessageKeys;

    public InputData(Validation validation,
                     List<String> value) {
        List<String> valueWithDefault = Objects.requireNonNullElseGet(value, List::of);
        this.value = valueWithDefault;
        this.valid = validation.apply(valueWithDefault);
        this.valueMessageKeys = Map.of();
    }

    InputData() {
        this(true, emptyList(), Map.of());
    }

    public InputData(@NotNull List<String> value) {
        this(true, value, Map.of());
    }

    private InputData(@NotNull Boolean valid,
                      @NotNull List<String> value,
                      @NotNull Map<String, String> valueMessageKeys) {
        this.valid = valid;
        this.value = value;
        this.valueMessageKeys = valueMessageKeys;
    }

    @SuppressWarnings("unused")
    public List<String> getMessageKeysForValue() {
        return value.stream()
                .map(value -> valueMessageKeys.getOrDefault(value, value))
                .collect(Collectors.toList());
    }
}
