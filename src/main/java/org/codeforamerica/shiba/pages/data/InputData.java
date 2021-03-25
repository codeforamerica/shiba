package org.codeforamerica.shiba.pages.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.shiba.pages.config.Validator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Value
@Builder
public class InputData {
    @NotNull List<String> value;
    @NotNull @JsonIgnore
    List<Validator> validators;

    InputData(List<String> value,
              @NotNull List<Validator> validators) {
        this.value = Objects.requireNonNullElseGet(value, List::of);
        this.validators = Objects.requireNonNullElseGet(validators, List::of);
    }

    InputData() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public InputData(@NotNull List<String> value) {
        this(value, new ArrayList<>());
    }

    public Boolean valid() {
        return this.validators.stream()
                .map(Validator::getValidation)
                .allMatch(validation -> validation.apply(this.value));
    }

    public Optional<String> errorMessageKey() {
        return this.validators.stream()
                .filter(validator -> !validator.getValidation().apply(this.value))
                .findFirst()
                .map(Validator::getErrorMessageKey);
    }

    public String getValue(int i) {
        return this.getValue().get(i);
    }

    public void setValue(String newValue, int i) {
        this.value.set(i, newValue);
    }
}
