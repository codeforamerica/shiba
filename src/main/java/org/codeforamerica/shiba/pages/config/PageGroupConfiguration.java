package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.output.Condition;

import java.util.List;
import java.util.Map;

@Data
public class PageGroupConfiguration {
    private List<String> completePages;
    private List<String> startPages;
    private String reviewPage;
    private String deleteWarningPage;
    private String redirectPage;
    private String restartPage;
    private Integer startingCount;
    private Map<String, Condition> addedScope;
}
