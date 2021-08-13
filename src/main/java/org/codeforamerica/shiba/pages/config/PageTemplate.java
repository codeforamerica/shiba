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
    AlertBox alertBox;
    @SuppressWarnings("unused")
    public boolean hasHeader() {
        return StringUtils.isNotBlank(headerKey);
    }

    @SuppressWarnings("unused")
    public boolean hasHeaderHelpMessageKey() {
        return StringUtils.isNotBlank(headerHelpMessageKey);
    }

    public boolean hasAlertBox() {
        return alertBox != null;
    }

    @SuppressWarnings("unused")
    public boolean hasSubtleLinkTextKey() {
        return StringUtils.isNotBlank(subtleLinkTextKey);
    }

    public boolean isSingleCheckboxOrRadioInputPage() {
        return inputs.size() == 1 && (inputs.get(0).getType() == FormInputType.CHECKBOX
                                     || inputs.get(0).getType() == FormInputType.RADIO
                                     || inputs.get(0).getType() == FormInputType.PEOPLE_CHECKBOX);
    }
}
