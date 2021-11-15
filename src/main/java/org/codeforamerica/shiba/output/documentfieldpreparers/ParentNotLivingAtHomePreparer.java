package org.codeforamerica.shiba.output.documentfieldpreparers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

@Component
public class ParentNotLivingAtHomePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    Map<String, String> idToChild = application.getApplicationData().getPagesData()
        .safeGetPageInputValue("childrenInNeedOfCare", "whoNeedsChildCare").stream()
        .collect(Collectors.toMap(FullNameFormatter::getId, FullNameFormatter::format));

    PageData pageData = application.getApplicationData().getPageData("parentNotAtHomeNames");

    if (pageData == null) {
      return List.of();
    }

    List<String> parentNames = pageData.get("whatAreTheParentsNames").getValue();
    List<String> childIds = pageData.get("childIdMap").getValue();

    List<DocumentField> result = new ArrayList<>();
    for (int i = 0; i < childIds.size(); i++) {
      String parentName = parentNames.get(i);
      String childId = childIds.get(i);

      result.add(new DocumentField(
          "custodyArrangement",
          "parentNotAtHomeName",
          List.of(parentName),
          DocumentFieldType.SINGLE_VALUE, i));
      result.add(new DocumentField(
          "custodyArrangement",
          "childFullName",
          List.of(idToChild.get(childId)),
          DocumentFieldType.SINGLE_VALUE, i));
    }
    return result;
  }
}
