package org.codeforamerica.shiba.pages.config;

import lombok.Data;

@Data
public class PageDatasource {
    private String pageName;
    private String groupName;
    private boolean optional = false;
}
