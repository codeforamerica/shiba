package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SelectableOptionsTemplate implements OptionsWithDataSourceTemplate {
    List<Option> selectableOptions = new ArrayList<>();
}
