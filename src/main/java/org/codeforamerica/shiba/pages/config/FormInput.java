package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

import java.util.Collections;
import java.util.List;

@Data
public class FormInput {
    private FormInputType type;
    private String name;
    private String customInputFragment;
    private PromptMessage promptMessage;
    private String helpMessageKey;
    private String validationErrorMessageKey;
    private List<Option> options;
    private List<FormInput> followUps = Collections.emptyList();
    private String followUpsValue;
    private Validator validator;
    private Boolean readOnly = false;
    private String defaultValue;
    private Integer max;
    private Integer min;
    private Condition condition;
}
