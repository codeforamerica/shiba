package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PAID_BY_THE_HOUR;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class PayPeriodInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {

    List<String> paidByHour = getValues(JOBS, PAID_BY_THE_HOUR, application.getApplicationData());
    if (paidByHour == null) {
      return Collections.emptyList();
    }

    List<ApplicationInput> result = new ArrayList<>();
    for (int i = 0; i < paidByHour.size(); i++) {
      if (Boolean.parseBoolean(paidByHour.get(i))) {
        result.add(new ApplicationInput("payPeriod", "payPeriod", List.of("Hourly"),
            ApplicationInputType.SINGLE_VALUE, i));
      }
    }

    return result;
  }
}
