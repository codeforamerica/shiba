package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.List;

@Data
public class PageGroupConfiguration {
    private List<String> completePages;
    private List<String> startPages;
    private String reviewPage;
    private String deleteWarningPage;
    private String redirectPage;
    private String restartPage;
    private Integer startingCount;
}
