package org.codeforamerica.shiba.pages.config;

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
    String subtleLinkTextKey;
    String subtleLinkTargetPage;
    Boolean hasPrimaryButton;
    String contextFragment;

    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return !this.headerKey.isBlank();
    }

    @SuppressWarnings("unused")
    public boolean hasHeaderHelpMessageKey() {
        return !this.headerHelpMessageKey.isBlank();
    }

    @SuppressWarnings("unused")
    public boolean hasSubtleLinkTextKey() {
        return !this.subtleLinkTextKey.isBlank();
    }
}
