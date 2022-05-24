package org.codeforamerica.shiba.pages;

import java.util.List;
import org.codeforamerica.shiba.output.caf.ExpeditedCcap;
import org.codeforamerica.shiba.output.caf.ExpeditedSnap;
import org.springframework.stereotype.Service;

@Service
public class NextStepsContentService {

  public List<String> getNextSteps(List<String> programs,
      ExpeditedSnap expeditedSnap,
      ExpeditedCcap expeditedCcap) {

    return List.of();
  }
}
