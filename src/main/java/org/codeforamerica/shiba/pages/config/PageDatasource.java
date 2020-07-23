package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.List;

@Data
public class PageDatasource {
    private String pageName;
    private List<InputDatasource> inputs = List.of();
}
