package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RACE_AND_ETHNICITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class HispanicLatinoOrSpanishPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {

    PagesData pagesData = application.getApplicationData().getPagesData();
    List<String> raceAndEthnicityValues = getValues(pagesData, RACE_AND_ETHNICITY);

    List<DocumentField> hispanicLatinoOrSpanishDocumentField = new ArrayList<>();

    if (pagesData.containsKey("raceAndEthnicity")) {
      if (raceAndEthnicityValues.contains("HISPANIC_LATINO_OR_SPANISH")) {
        hispanicLatinoOrSpanishDocumentField.add(
            new DocumentField("raceAndEthnicity", "HISPANIC_LATINO_OR_SPANISH", "true",
                ENUMERATED_SINGLE_VALUE));
      } else if (!raceAndEthnicityValues.contains("HISPANIC_LATINO_OR_SPANISH")) {
        hispanicLatinoOrSpanishDocumentField.add(
            new DocumentField("raceAndEthnicity", "HISPANIC_LATINO_OR_SPANISH_NO", "true",
                ENUMERATED_SINGLE_VALUE));
      }
    }
    return hispanicLatinoOrSpanishDocumentField;
  }
}
