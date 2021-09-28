package org.codeforamerica.shiba.output.applicationinputsmappers;

import static java.lang.Boolean.parseBoolean;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENERGY_ASSISTANCE_OVER_20;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RECEIVES_ENERGY_ASSISTANCE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_SINGLE_VALUE;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class EnergyAssistanceInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  private List<ApplicationInput> map(PagesData pagesData) {
    String energyAssistance = getFirstValue(pagesData, RECEIVES_ENERGY_ASSISTANCE);

    // Part of response was left blank
    if (energyAssistance == null) {
      return Collections.emptyList();
    }

    boolean receivedLiheap = parseBoolean(energyAssistance);
    String receivedMoreThan20 = getFirstValue(pagesData, ENERGY_ASSISTANCE_OVER_20);

    // Part of response was left blank
    if (receivedLiheap && receivedMoreThan20 == null) {
      return Collections.emptyList();
    }

    return List.of(
        new ApplicationInput("energyAssistanceGroup", "energyAssistanceInput",
            String.valueOf(receivedLiheap && parseBoolean(receivedMoreThan20)),
            ENUMERATED_SINGLE_VALUE));
  }
}
