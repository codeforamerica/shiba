package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class HouseholdPregnancyPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<String> pregnantHouseholdMembers = getListOfSelectedFullNames(application, "whoIsPregnant",
        "whoIsPregnant");

    return List.of(
        new DocumentField("householdPregnancy", "householdPregnancy",
            List.of(String.join(", ", pregnantHouseholdMembers)), DocumentFieldType.SINGLE_VALUE,
            null)
    );
  }
}
