package org.codeforamerica.shiba.pages.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SelectableOptionsTemplate implements OptionsWithDataSourceTemplate, Serializable {

  @Serial
  private static final long serialVersionUID = 5831139291290273630L; 

  List<Option> selectableOptions = new ArrayList<>();
}
