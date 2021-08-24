package org.codeforamerica.shiba.output;

import java.util.Optional;
import lombok.Data;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.ApplicationData;

@Data
public class DerivedValue {

  private DerivedValueConfiguration value;
  private ApplicationInputType type;
  private Condition condition;

  public boolean shouldDeriveValue(ApplicationData applicationData) {
    return Optional.ofNullable(condition)
        .map(compositeCondition -> compositeCondition.appliesTo(applicationData))
        .orElse(true);
  }
}
