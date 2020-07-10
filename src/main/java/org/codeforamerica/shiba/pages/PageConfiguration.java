package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codeforamerica.shiba.pages.FormData.getFormDataFrom;

@Data
public class PageConfiguration {
    public List<FormInput> inputs = List.of();
    private Value pageTitle;
    private Value headerKey;
    private String headerHelpMessageKey;
    private List<PageDatasource> datasources = new ArrayList<>();
    private String primaryButtonTextKey = "general.continue";
    private List<AdditionalDatum> additionalData = List.of();

    public String resolve(PagesData pagesData, Value value) {
        return resolve(value, datasourceCondition(pagesData));
    }

    public String resolve(MultiValueMap<String, String> model, Value value) {
        return resolve(value, modelCondition(model));
    }

    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return this.headerKey != null;
    }

    @SuppressWarnings("unused")
    public boolean displayContinueButton() {
        return this.inputs.stream().noneMatch(input -> input.getType().equals(FormInputType.YES_NO));
    }

    public List<FormInput> getFlattenedInputs() {
        return this.inputs.stream()
                .flatMap(formInput -> Stream.concat(Stream.of(formInput), formInput.followUps.stream()))
                .collect(Collectors.toList());
    }

    boolean isStaticPage() {
        return this.inputs.isEmpty();
    }

    private Function<Condition, Boolean> datasourceCondition(PagesData pagesData) {
        return condition -> {
            Objects.requireNonNull(this.getDatasources(),
                    "Configuration mismatch! Conditional value cannot be evaluated without a datasource.");
            FormData formData = getFormDataFrom(this.getDatasources(), pagesData);
            return condition.appliesTo(formData);
        };
    }

    private Function<Condition, Boolean> modelCondition(MultiValueMap<String, String> model) {
        return condition -> condition.appliesTo(model);
    }

    private String resolve(Value value, Function<Condition, Boolean> conditionFunction) {
        if (value == null) {
            return "";
        }

        return value.getConditionalValues().stream()
                .filter(conditionalValue -> {
                    Condition condition = conditionalValue.getCondition();
                    return conditionFunction.apply(condition);
                })
                .findFirst()
                .map(ConditionalValue::getValue)
                .orElse(value.getValue());
    }
}
