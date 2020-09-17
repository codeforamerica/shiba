package org.codeforamerica.shiba.output.caf;

import lombok.Data;

@Data
public class PageInputCoordinates {
    private String pageName;
    private String inputName;
    private String defaultValue;
    private Boolean required = false;
}
