package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OptionsWithDataSource {
    List<PageDatasource> datasources = new ArrayList<>();
    List<Option> selectableOptions = new ArrayList<>();
}
