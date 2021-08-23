package org.codeforamerica.shiba.pages.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

@Data
public class ReferenceOptionsTemplate implements OptionsWithDataSourceTemplate {

  Map<String, Subworkflow> subworkflows = new HashMap<>();
  Map<String, PageData> datasources = new HashMap<>();
}
