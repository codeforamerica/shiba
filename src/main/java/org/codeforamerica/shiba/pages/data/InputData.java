package org.codeforamerica.shiba.pages.data;

import lombok.Builder;
import lombok.Value;
import org.codeforamerica.shiba.pages.config.Validator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Value
@Builder
public class InputData {
    @NotNull List<String> value;
    @NotNull List<Validator> validators;

    InputData(List<String> value,
              @NotNull List<Validator> validators) {
        this.value = Objects.requireNonNullElseGet(value, List::of);
        this.validators = validators;
    }

    InputData() {
        this(emptyList(), emptyList());
    }

    public InputData(@NotNull List<String> value) {
        this(value, emptyList());
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
}
