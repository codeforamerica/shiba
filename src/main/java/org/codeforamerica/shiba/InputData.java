package org.codeforamerica.shiba;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Value
public class InputData {
    @NotNull Boolean valid;
    @NotNull List<String> value;
    @NotNull Map<String, String> valueMessageKeys;

    InputData(Validation validation,
              List<String> value) {
        List<String> valueWithDefault = Objects.requireNonNullElseGet(value, List::of);
        this.value = valueWithDefault;
        this.valid = validation.apply(valueWithDefault);
        this.valueMessageKeys = Map.of();
    }

    InputData() {
        this(true, emptyList(), Map.of());
    }

    private InputData(@NotNull Boolean valid,
                      @NotNull List<String> value,
                      @NotNull Map<String, String> valueMessageKeys) {
        this.valid = valid;
        this.value = value;
        this.valueMessageKeys = valueMessageKeys;
    }

    @SuppressWarnings("unused")
    public List<String> getValueMessageKey() {
        return value.stream()
                .map(value -> valueMessageKeys.getOrDefault(value, value))
                .collect(Collectors.toList());
    }

    InputData withValueMessageKeys(@NotNull Map<String, String> valueMessageKeys) {
        return new InputData(this.valid, this.value, valueMessageKeys);
    }
}
