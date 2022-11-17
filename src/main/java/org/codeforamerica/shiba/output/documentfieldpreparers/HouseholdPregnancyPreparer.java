package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_PREGNANT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class HouseholdPregnancyPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<String> pregnantHouseholdMembers = getListOfSelectedFullNames(application, "whoIsPregnant",
        "whoIsPregnant");
    PagesData data = application.getApplicationData().getPagesData();
    Boolean anyoneInHouseholdPregnant = Boolean.valueOf(getFirstValue(data, IS_PREGNANT));
    String applicantName = FullNameFormatter.getFullName(application);
    
    List<DocumentField> results = new ArrayList<DocumentField>();
    
    results.add(new DocumentField("householdPregnancy", "householdPregnancy",
            List.of(String.join(", ", pregnantHouseholdMembers)), DocumentFieldType.SINGLE_VALUE,
            null));
    if (pregnantHouseholdMembers.contains(applicantName) || (anyoneInHouseholdPregnant && pregnantHouseholdMembers.contains(""))) {
    	results.add(new DocumentField("pregnant", "applicantIsPregnant",
                "Yes", DocumentFieldType.SINGLE_VALUE));
    }
    else {
    	results.add(new DocumentField("pregnant", "applicantIsPregnant",
                "No", DocumentFieldType.SINGLE_VALUE));
    }
    
    return results;
  }
}
