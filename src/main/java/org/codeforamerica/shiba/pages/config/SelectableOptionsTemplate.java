package org.codeforamerica.shiba.pages.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SelectableOptionsTemplate implements OptionsWithDataSourceTemplate {

  List<Option> selectableOptions = new ArrayList<>();
}
