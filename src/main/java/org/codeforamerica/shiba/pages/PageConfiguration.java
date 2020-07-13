package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.springframework.util.MultiValueMap;

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
    private List<String> nextPage;
    private String previousPage;
    private List<PageDatasource> datasources;
    private Condition skipCondition;
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

    boolean shouldSkip(PagesData pagesData) {
        if (this.datasources == null || this.skipCondition == null) {
            return false;
        }
        return this.skipCondition.appliesTo(getFormDataFrom(datasources, pagesData));
    }

    String getAdjacentPageName(boolean isBackwards) {
        return getAdjacentPageName(isBackwards, 0);
    }

    public String getAdjacentPageName(boolean isBackwards, Integer option) {
        if (isBackwards) {
            return previousPage;
        }
        return nextPage.get(option);
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
