package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.springframework.stereotype.Component;

@Component
public class LivingWithFriendsPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
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
        new DocumentField(
            "livingWithFamilyOrFriendsYesNo",
            "livingWithFamilyOrFriendsYesNo",
            List.of(inputValue),
            DocumentFieldType.SINGLE_VALUE,
            null
        )
    );
  }
}
