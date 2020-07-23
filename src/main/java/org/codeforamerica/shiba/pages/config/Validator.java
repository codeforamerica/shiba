package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

@Data
public class Validator {
    private Validation validation = Validation.NONE;
    private Condition condition;
}
