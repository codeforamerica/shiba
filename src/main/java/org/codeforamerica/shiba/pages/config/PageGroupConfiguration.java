package org.codeforamerica.shiba.pages.config;

import lombok.Data;

@Data
public class PageGroupConfiguration {
    private String noDataRedirectPage;
    private String completePage;
    private String startPage;
    private String redirectPage;
    private String restartPage;
}
