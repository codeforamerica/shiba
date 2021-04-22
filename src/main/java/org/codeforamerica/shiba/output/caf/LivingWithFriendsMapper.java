package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LivingWithFriendsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Document document, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        List<String> pageInputValue = application.getApplicationData().getPagesData().safeGetPageInputValue("livingSituation", "livingSituation");
        if (pageInputValue.size() == 0) {
            return List.of();
        }
        String livingSituation = pageInputValue.get(0);

        boolean livingWithFamilyOrFriendsDueToEconomicHardship = livingSituation.contains("TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP");
        boolean livingWithFamilyOrFriendsDueToOtherReasons = livingSituation.contains("TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS");

        if (livingWithFamilyOrFriendsDueToEconomicHardship) {
            return List.of(
                    new ApplicationInput(
                            "livingWithFamilyOrFriendsYesNo",
                            "livingWithFamilyOrFriendsYesNo",
                            List.of("Yes"),
                            ApplicationInputType.SINGLE_VALUE,
                            null
                    )
            );
        } else if (livingWithFamilyOrFriendsDueToOtherReasons) {
            return List.of(
                    new ApplicationInput(
                            "livingWithFamilyOrFriendsYesNo",
                            "livingWithFamilyOrFriendsYesNo",
                            List.of("No"),
                            ApplicationInputType.SINGLE_VALUE,
                            null
                    )
            );
        } else {
            return List.of(
                    new ApplicationInput(
                            "livingWithFamilyOrFriendsYesNo",
                            "livingWithFamilyOrFriendsYesNo",
                            List.of("Off"),
                            ApplicationInputType.SINGLE_VALUE,
                            null
                    )
            );
        }
    }
}