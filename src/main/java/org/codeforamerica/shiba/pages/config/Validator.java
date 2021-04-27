package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.output.Condition;

import java.io.*;

@Data
public class Validator implements Serializable {
    @Serial
    private static final long serialVersionUID = -6670512506564832396L;

    private Validation validation = Validation.NONE;
    private String errorMessageKey;
    private Condition condition;
}
