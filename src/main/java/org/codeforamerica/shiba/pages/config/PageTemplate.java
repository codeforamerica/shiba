package org.codeforamerica.shiba.pages.config;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

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
        return StringUtils.isNotBlank(headerKey);
    }

    @SuppressWarnings("unused")
    public boolean hasHeaderHelpMessageKey() {
        return StringUtils.isNotBlank(headerHelpMessageKey);
    }

    @SuppressWarnings("unused")
    public boolean hasSubtleLinkTextKey() {
        return StringUtils.isNotBlank(subtleLinkTextKey);
    }
}
