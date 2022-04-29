package org.codeforamerica.shiba.pages;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.stereotype.Service;

@Service
public class NextStepsContentService {

  public List<String> getNextSteps(List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility) {

    return List.of();
  }
}
