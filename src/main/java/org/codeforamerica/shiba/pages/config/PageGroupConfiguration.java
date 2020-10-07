package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.List;

@Data
public class PageGroupConfiguration {
    private String noDataRedirectPage;
    private List<String> completePages;
    private String startPage;
    private String redirectPage;
    private String restartPage;
    private Integer startingCount;
}
