package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.List;

@Data
public class PageDatasource {
    String pageName;
    List<InputDatasource> inputs = List.of();
}
