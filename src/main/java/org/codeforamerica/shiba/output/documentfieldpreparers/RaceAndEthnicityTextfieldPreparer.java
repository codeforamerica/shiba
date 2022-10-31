package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RACE_AND_ETHNICITY;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class RaceAndEthnicityTextfieldPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
	  
    PagesData pagesData = application.getApplicationData().getPagesData();
    List<String> raceAndEthnicityValues = getValues(pagesData, RACE_AND_ETHNICITY);
    List<DocumentField> results = new ArrayList<DocumentField>();
    StringJoiner joiner = new StringJoiner(", ");
    
    if (pagesData.containsKey("raceAndEthnicity")) {
        if (raceAndEthnicityValues.contains("MIDDLE_EASTERN_OR_NORTH_AFRICAN")) {
        	joiner.add("Middle Eastern / N. African");
        }
        if (raceAndEthnicityValues.contains("WHITE")) {
        	joiner.add("White");
        }
        if (raceAndEthnicityValues.contains("ASIAN")) {
        	joiner.add("Asian");
        }
        if (raceAndEthnicityValues.contains("AMERICAN_INDIAN_OR_ALASKA_NATIVE")) {
        	joiner.add("American Indian or Alaska Native");
        }
        if (raceAndEthnicityValues.contains("BLACK_OR_AFRICAN_AMERICAN")) {
        	joiner.add("Black or African American");
        }
        if (raceAndEthnicityValues.contains("NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER")) {
        	joiner.add("Native Hawaiian or Pacific Islander");
        }
        if (raceAndEthnicityValues.contains("HISPANIC_LATINO_OR_SPANISH")) {
        	joiner.add("Hispanic, Latino, or Spanish");
        }
        if (raceAndEthnicityValues.contains("SOME_OTHER_RACE_OR_ETHNICITY") ) {
        	joiner.add(pagesData.getPageInputFirstValue("raceAndEthnicity", "otherRaceOrEthnicity"));
        } 
      }
    
    results.add(new DocumentField("raceAndEthnicity", "applicantRaceAndEthnicity", joiner.toString(), DocumentFieldType.SINGLE_VALUE));
    
    return results;
  }
}
