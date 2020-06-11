package org.codeforamerica.shiba;

import lombok.Data;

import java.util.List;

@Data
public class PageDatasource {
    String screenName;
    List<InputDatasource> inputs = List.of();
}
