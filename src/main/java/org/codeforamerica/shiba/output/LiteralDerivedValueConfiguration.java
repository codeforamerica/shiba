package org.codeforamerica.shiba.output;

import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.codeforamerica.shiba.pages.data.ApplicationData;

@Data
public class LiteralDerivedValueConfiguration implements DerivedValueConfiguration {

  private String literal;

  @Override
  public Optional<List<String>> resolveOptional(ApplicationData data) {
    return Optional.of(List.of(literal));
  }
}
