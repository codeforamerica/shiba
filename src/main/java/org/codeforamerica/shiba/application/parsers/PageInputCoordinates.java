package org.codeforamerica.shiba.application.parsers;

import lombok.Data;

@Data
public class PageInputCoordinates {
    private String pageName;
    private String inputName;
    private String defaultValue;
    private String groupName;
    private Boolean required = false;
}
