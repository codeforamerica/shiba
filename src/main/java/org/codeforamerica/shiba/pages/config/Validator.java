package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;

import java.io.*;

@Data
public class Validator implements Serializable {
    @Serial
    private static final long serialVersionUID = -644544878960451235L;

    private Validation validation = Validation.NONE;
    private String errorMessageKey;
    private Condition condition;
}
