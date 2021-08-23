package org.codeforamerica.shiba.pages.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.shiba.pages.config.Validator;
import org.jetbrains.annotations.NotNull;

@Value
@Builder
public class InputData implements Serializable {

  @Serial
  private static final long serialVersionUID = 8511070147741948268L;

  @NotNull List<String> value;
  @NotNull
  @JsonIgnore
  List<Validator> validators;

  InputData(List<String> value, @NotNull List<Validator> validators) {
    this.value = Objects.requireNonNullElseGet(value, List::of);
    this.validators = Objects.requireNonNullElseGet(validators, List::of);
  }

  InputData() {
    this(new ArrayList<>(), new ArrayList<>());
  }

  public InputData(@NotNull List<String> value) {
    this(value, new ArrayList<>());
  }

  public Boolean valid(PageData pageData) {
    return validators.stream().filter(
        validator -> validator.getCondition() == null || validator.getCondition()
            .satisfies(pageData)).map(Validator::getValidation)
        .allMatch(validation -> validation.apply(value));
  }

  public List<String> errorMessageKeys(PageData pageData) {
    return validators.stream()
        .filter(validator -> (validator.getCondition() == null || validator.getCondition().satisfies(pageData)) && !validator.getValidation().apply(value))
        .map(Validator::getErrorMessageKey).collect(Collectors.toList());
  }

  public String getValue(int i) {
    return this.getValue().get(i);
  }

  public void setValue(String newValue, int i) {
    this.value.set(i, newValue);
  }
}
