package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.*;

@Component
public class CcapExpeditedEligibilityDecider {
    private final CcapExpeditedEligibilityParser ccapExpeditedEligibilityParser;
    private static final Set<String> expeditedLivingSituations
            = Set.of("HOTEL_OR_MOTEL", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY", "EMERGENCY_SHELTER", "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING");

    public CcapExpeditedEligibilityDecider(CcapExpeditedEligibilityParser ccapExpeditedEligibilityParser) {
        this.ccapExpeditedEligibilityParser = ccapExpeditedEligibilityParser;
    }

    public CcapExpeditedEligibility decide(ApplicationData applicationData) {
        return ccapExpeditedEligibilityParser.parse(applicationData)
                .map(parameters -> {
                            String livingSituation = parameters.getLivingSituation();
                            if (null == livingSituation || !parameters.isCcapApplication()) {
                                return UNDETERMINED;
                            }
                            return expeditedLivingSituations.contains(livingSituation) ? ELIGIBLE : NOT_ELIGIBLE;
                        }
                ).orElse(UNDETERMINED);
    }
}
