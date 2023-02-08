package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_SSN;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SSNYesNoPreparer implements DocumentFieldPreparer { 
  
@Override
public List<DocumentField> prepareDocumentFields(Application application, Document document,
    Recipient recipient) {
  var houseHoldInfo = getHouseHoldInfo(application, recipient);
  return houseHoldInfo;

}

private List<DocumentField> getHouseHoldInfo(Application application, Recipient recipient) {
  var householdSubworkflow =
      ofNullable(getGroup(application.getApplicationData(), Group.HOUSEHOLD));
  return householdSubworkflow.map(subworkflow -> getApplicationInputsForSubworkflow(subworkflow, recipient)).orElse(emptyList());
}


@NotNull
private List<DocumentField> getApplicationInputsForSubworkflow(Subworkflow subworkflow, Recipient recipient) {
List<Iteration> houseHoldInfoList = subworkflow;
int index = 0;
List<DocumentField> inputsForSubworkflow = new ArrayList<>();
for (Iteration i : houseHoldInfoList) {
  var pageData = i.getPagesData();
  var pgSSN = getFirstValue(pageData, HOUSEHOLD_INFO_SSN);
  if (pgSSN.length() > 0) {
    inputsForSubworkflow
        .add(new DocumentField("householdMemberInfo", "ssnYesNo", "Yes", SINGLE_VALUE, index));
  }
  index++;
}
return inputsForSubworkflow;
}
}
