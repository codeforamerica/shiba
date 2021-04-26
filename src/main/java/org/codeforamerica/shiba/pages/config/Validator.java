package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.output.Condition;

import java.io.Serializable;

@Data
public class Validator implements Serializable {
    private Validation validation = Validation.NONE;
    private String errorMessageKey;
    private Condition condition;
}
