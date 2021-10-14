package org.codeforamerica.shiba.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class NextStepsContentService {

  private final String PHONE_ICON = "fragments/icons/icon-phone :: icon-phone";
  private final String LETTER_ICON = "fragments/icons/icon-letter :: icon-letter";
  private final String SILHOUETTE_ICON = "fragments/icons/icon-communicate :: icon-communicate";

  public List<NextStepSection> getNextSteps(List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility) {

    return new ArrayList<>();
  }

  public record NextStepSection(String icon, String message) {

  }
}
