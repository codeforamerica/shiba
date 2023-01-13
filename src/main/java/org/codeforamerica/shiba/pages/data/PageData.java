package org.codeforamerica.shiba.pages.data;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.pages.PageUtils.getFormInputName;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.Validator;
import org.springframework.util.MultiValueMap;

/**
 * PageData extends HashMap&lt;String, InputData&gt;
 *
 */
@EqualsAndHashCode(callSuper = true)
@Value
@NoArgsConstructor
public class PageData extends HashMap<String, InputData> {

  @Serial
  private static final long serialVersionUID = -1930835377533297692L;

  public PageData(Map<String, InputData> inputDataMap) {
    super(inputDataMap);
  }

  public static PageData fillOut(PageConfiguration page, MultiValueMap<String, String> model) {
    Map<String, InputData> inputDataMap = page.getFlattenedInputs()
        .stream()
        .map(formInput -> {
          List<String> value = ofNullable(model)
              .map(modelMap -> modelMap.get(getFormInputName(formInput.getName())))
              .orElse(emptyList());

          // Remove unicode null character if present
          List<String> sanitizedValue = value.stream()
              .map(v -> v.replace("\u0000", ""))
              .collect(Collectors.toCollection(ArrayList::new));
          InputData inputData = new InputData(sanitizedValue, formInput.getValidators());
          return Map.entry(formInput.getName(), inputData);
        })
        .collect(toMap(Entry::getKey, Entry::getValue));
    return new PageData(inputDataMap);
  }

  public static PageData initialize(PageConfiguration pageConfiguration) {
    return new PageData(
        pageConfiguration.getFlattenedInputs().stream()
            .collect(toMap(
                FormInput::getName,
                input -> ofNullable(input.getDefaultValue())
                    .map(defaultValue -> new InputData(List.of(defaultValue)))
                    .orElse(new InputData())
            )));
  }

  public Boolean isValid() {
    Predicate<Validator> validatorForThisInputShouldRun = validator -> ofNullable(
        validator.getCondition()).map(
        condition -> condition.satisfies(this)
    ).orElse(true);

    List<InputData> inputDataToValidate = values().stream().filter(
        inputData -> inputData.getValidators().stream().anyMatch(validatorForThisInputShouldRun)
    ).toList();

    return inputDataToValidate.stream().allMatch(inputData -> inputData.valid(this));
  }

  /**
   * Merges the InputData values of otherPage into this PageData.
   *
   * @param otherPage PageData containing values to merge.
   */
  public void mergeInputDataValues(PageData otherPage) {
    if (otherPage != null) {
      otherPage.forEach((key, value) -> {
        putIfAbsent(key, new InputData(new ArrayList<>()));
        get(key).getValue().addAll(value.getValue());
      });
    }
  }

  public String invalidPageDataLogText() {

    if (isValid()) {
      return "";
    }

    Predicate<Validator> validatorForThisInputShouldRun = validator -> ofNullable(
            validator.getCondition()).map(
            condition -> condition.satisfies(this)
    ).orElse(true);

    List<InputData> inputDataToValidate = values().stream().filter(
            inputData -> inputData.getValidators().stream().anyMatch(validatorForThisInputShouldRun)
    ).collect(Collectors.toList());

    List<InputData> validInput = inputDataToValidate.stream()
            .filter(inputData -> inputData.valid(this))
            .collect(Collectors.toList());

    // Use a copy of the page data and remove all valid entries so the invalid ones remain
    HashMap<String, InputData> invalidStore = new HashMap<>(this);
    invalidStore.values().removeAll(validInput);

    StringBuffer buffer = new StringBuffer();
    for( Map.Entry<String, InputData> entry : invalidStore.entrySet() ) {
      buffer.append(String.format("%s : %s", entry.getKey(), entry.getValue().toString()));
      buffer.append(", ");
    }

    return buffer.toString();
  }
}
