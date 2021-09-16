package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.ELIGIBLE;
import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.NOT_ELIGIBLE;
import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.UNDETERMINED;

import java.util.List;
import java.util.Set;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class CcapExpeditedEligibilityDecider {

  private static final Set<String> EXPEDITED_LIVING_SITUATIONS
      = Set.of("HOTEL_OR_MOTEL", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP",
      "EMERGENCY_SHELTER", "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING");

  public CcapExpeditedEligibility decide(ApplicationData applicationData) {
    if (applicationData.getPagesData().safeGetPageInputValue("expeditedCcap", "expeditedCcap").equals(
        List.of("true"))) {
      return ELIGIBLE;
    } else {
      return NOT_ELIGIBLE;
    }
  }
}