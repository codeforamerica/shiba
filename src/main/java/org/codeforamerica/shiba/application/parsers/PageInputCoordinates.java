package org.codeforamerica.shiba.application.parsers;

import lombok.Data;

@Data
public class PageInputCoordinates {
    private String pageName;
    private String inputName;
    private String defaultValue;
    private Boolean required = false;
}
