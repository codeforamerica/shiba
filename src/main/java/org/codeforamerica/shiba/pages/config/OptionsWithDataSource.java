package org.codeforamerica.shiba.pages.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OptionsWithDataSource {

  List<PageDatasource> datasources = new ArrayList<>();
  List<Option> selectableOptions = new ArrayList<>();
}
