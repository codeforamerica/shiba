package org.codeforamerica.shiba.pages;

import lombok.Value;
import org.codeforamerica.shiba.pages.config.FormInputType;

import java.util.List;

@Value
public class PageTemplate {
    List<FormInputTemplate> inputs;
    String pageTitle;
    String headerKey;
    String headerHelpMessageKey;
    String primaryButtonTextKey;

    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return !this.headerKey.isBlank();
    }

    @SuppressWarnings("unused")
    public boolean displayContinueButton() {
        return this.inputs.stream().noneMatch(input -> input.getType().equals(FormInputType.YES_NO));
    }

}
