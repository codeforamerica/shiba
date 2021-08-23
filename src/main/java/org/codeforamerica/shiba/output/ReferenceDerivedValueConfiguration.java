package org.codeforamerica.shiba.output;

import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;

@Data
public class ReferenceDerivedValueConfiguration implements DerivedValueConfiguration {

  private String pageName;
  private String inputName;

  @Override
  public Optional<List<String>> resolveOptional(ApplicationData data) {
    return Optional.ofNullable(data.getPageData(pageName))
        .flatMap(pageData -> Optional.ofNullable(pageData.get(inputName)))
        .map(InputData::getValue);
  }
}
