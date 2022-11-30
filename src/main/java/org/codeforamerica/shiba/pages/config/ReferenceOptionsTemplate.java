package org.codeforamerica.shiba.pages.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

@Data
public class ReferenceOptionsTemplate implements OptionsWithDataSourceTemplate, Serializable {

  @Serial
  private static final long serialVersionUID = 5831139204290272830L;

  Map<String, Subworkflow> subworkflows = new HashMap<>();
  Map<String, PageData> datasources = new HashMap<>();
}
