package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RACE_AND_ETHNICITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_MULTI_VALUE;

import io.sentry.protocol.App;
import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
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
            new ApplicationInput("hispanicOrLatino", "HISPANIC_LATINO_OR_SPANISH", "HISPANIC_LATINO_OR_SPANISH",
                ENUMERATED_MULTI_VALUE));
      } else if (!raceAndEthnicityValues.contains("HISPANIC_LATINO_OR_SPANISH")) {
        hispanicLatinoOrSpanishApplicationInput.add(
            new ApplicationInput("hispanicOrLatino", "HISPANIC_LATINO_OR_SPANISH", "No",
                ENUMERATED_MULTI_VALUE));
      }
    }
    return hispanicLatinoOrSpanishApplicationInput;
  }
}
