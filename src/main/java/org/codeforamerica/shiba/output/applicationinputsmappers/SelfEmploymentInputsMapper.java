package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class SelfEmploymentInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {

    List<String> selfEmploymentInputs = getValues(JOBS, IS_SELF_EMPLOYMENT,
        application.getApplicationData());

    if (selfEmploymentInputs == null) {
      return Collections.emptyList();
    }

    boolean hasSelfEmployedJob = selfEmploymentInputs.contains("true");

    // Is anyone in the household self-employed?
    return List.of(new ApplicationInput(
            "employee",
            "selfEmployed",
            hasSelfEmployedJob ? "true" : "false",
            ApplicationInputType.SINGLE_VALUE
        ),
        new ApplicationInput(
            "employee",
            "selfEmployedGrossMonthlyEarnings",
            hasSelfEmployedJob ? "see question 9" : "",
            ApplicationInputType.SINGLE_VALUE
        ));
  }
}
