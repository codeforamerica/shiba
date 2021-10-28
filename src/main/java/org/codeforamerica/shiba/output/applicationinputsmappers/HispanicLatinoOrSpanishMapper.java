package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RACE_AND_ETHNICITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class HispanicLatinoOrSpanishMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {

    PagesData pagesData = application.getApplicationData().getPagesData();
    List<String> raceAndEthnicityValues = getValues(pagesData, RACE_AND_ETHNICITY);

    List<ApplicationInput> hispanicLatinoOrSpanishApplicationInput = new ArrayList<>();

    if (pagesData.containsKey("raceAndEthnicity")) {
      if (raceAndEthnicityValues.contains("HISPANIC_LATINO_OR_SPANISH")) {
        hispanicLatinoOrSpanishApplicationInput.add(
            new ApplicationInput("raceAndEthnicity", "HISPANIC_LATINO_OR_SPANISH", "true",
                ENUMERATED_SINGLE_VALUE));
      } else if (!raceAndEthnicityValues.contains("HISPANIC_LATINO_OR_SPANISH")) {
        hispanicLatinoOrSpanishApplicationInput.add(
            new ApplicationInput("raceAndEthnicity", "HISPANIC_LATINO_OR_SPANISH_NO", "true",
                ENUMERATED_SINGLE_VALUE));
      }
    }
    return hispanicLatinoOrSpanishApplicationInput;
  }
}
