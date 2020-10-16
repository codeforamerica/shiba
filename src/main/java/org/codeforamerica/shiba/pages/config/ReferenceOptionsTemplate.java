package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

import java.util.HashMap;
import java.util.Map;

@Data
public class ReferenceOptionsTemplate implements OptionsWithDataSourceTemplate {
    Map<String, Subworkflow> subworkflows = new HashMap<>();
    Map<String, PageData> datasources = new HashMap<>();
}
