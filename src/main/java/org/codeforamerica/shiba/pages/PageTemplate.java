package org.codeforamerica.shiba.pages;

import lombok.Value;

import java.util.List;

@Value
public class PageTemplate {
    List<FormInputTemplate> inputs;
    String name;
    String pageTitle;
    String headerKey;
    String headerHelpMessageKey;
    String primaryButtonTextKey;
    Boolean hasPrimaryButton;

    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return !this.headerKey.isBlank();
    }
}
