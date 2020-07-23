package org.codeforamerica.shiba.pages.config;

import lombok.Data;

@Data
public class Option {
    private String value;
    private String messageKey;
    private Boolean isNone;
}
