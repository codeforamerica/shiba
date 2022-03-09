package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RACE_AND_ETHNICITY;
import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class MiddleEasternNorthAfricanWhiteAndOthersPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {

    PagesData pagesData = application.getApplicationData().getPagesData();
    List<String> raceAndEthnicityValues = getValues(pagesData, RACE_AND_ETHNICITY);
    String otherRaceAndEthnicityValue = pagesData.getPageInputFirstValue("raceAndEthnicity", "otherRaceOrEthnicity");  
   
    List<DocumentField> middleEasternNorthAfricanDocumentField = new ArrayList<>();

    if (pagesData.containsKey("raceAndEthnicity")) {
      if (raceAndEthnicityValues.size() == 1 && raceAndEthnicityValues.contains("MIDDLE_EASTERN_OR_NORTH_AFRICAN")) {
        middleEasternNorthAfricanDocumentField.add(
            new DocumentField("raceAndEthnicity", "CLIENT_REPORTED", "Middle Eastern / N. African",
                DocumentFieldType.SINGLE_VALUE));
        middleEasternNorthAfricanDocumentField.add(
            new DocumentField("raceAndEthnicity", "WHITE", "true",
                DocumentFieldType.ENUMERATED_SINGLE_VALUE));
      }
      if (raceAndEthnicityValues.contains("WHITE")) {
        middleEasternNorthAfricanDocumentField.add(
            new DocumentField("raceAndEthnicity", "WHITE", "true",
                DocumentFieldType.ENUMERATED_SINGLE_VALUE));
      }  
      if (raceAndEthnicityValues.contains("SOME_OTHER_RACE_OR_ETHNICITY") ) {
        middleEasternNorthAfricanDocumentField.add(
            new DocumentField("raceAndEthnicity", "CLIENT_REPORTED", otherRaceAndEthnicityValue,
                DocumentFieldType.SINGLE_VALUE));
      } 
    }
    return middleEasternNorthAfricanDocumentField;
  }
}
