package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class PageConfiguration {
    public List<FormInput> inputs = List.of();
    private Value pageTitle;
    private Value headerKey;
    private String headerHelpMessageKey;
    private String primaryButtonTextKey = "general.continue";
    private List<AdditionalDatum> additionalData = List.of();

    public String resolve(MultiValueMap<String, String> model, Value value) {
        if (value == null) {
            return "";
        }
        return value.resolve(condition -> condition.appliesTo(model));
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

}
