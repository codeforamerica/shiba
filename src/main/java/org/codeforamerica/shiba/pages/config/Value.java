package org.codeforamerica.shiba.pages.config;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Value {

  private String defaultValue;
  private List<ConditionalValue> conditionalValues = List.of();

  public Value(String defaultValue) {
    this.defaultValue = defaultValue;
  }
}
