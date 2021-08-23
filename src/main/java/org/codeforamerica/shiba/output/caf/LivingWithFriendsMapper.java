package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.springframework.stereotype.Component;

@Component
public class LivingWithFriendsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    List<String> pageInputValue = application.getApplicationData().getPagesData()
        .safeGetPageInputValue("livingSituation", "livingSituation");
    if (pageInputValue.size() == 0) {
      return List.of();
    }
    String livingSituation = pageInputValue.get(0);

    var inputValue = "Off";
    if (livingSituation.contains("TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP")) {
      inputValue = "Yes";
    } else if (livingSituation.contains("TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS")) {
      inputValue = "No";
    }

    return List.of(
        new ApplicationInput(
            "livingWithFamilyOrFriendsYesNo",
            "livingWithFamilyOrFriendsYesNo",
            List.of(inputValue),
            ApplicationInputType.SINGLE_VALUE,
            null
        )
    );
  }
}
