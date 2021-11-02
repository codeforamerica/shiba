package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.LIVING_SITUATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class LivingSituationInputsMapper implements ApplicationInputsMapper {

  List<String> TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OPTIONS = List.of(
      "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP",
      "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OTHER_REASONS");

  @Override
  public List<ApplicationInput> map(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  private List<ApplicationInput> map(PagesData pagesData) {
    // Question was unanswered
    if (pagesData.get("livingSituation") == null) {
      return Collections.emptyList();
    }

    String livingSituation = getFirstValue(pagesData, LIVING_SITUATION);

    // Answer was left blank
    if (livingSituation == null) {
      return List.of(createApplicationInput("UNKNOWN"));
    }

    if (TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OPTIONS.contains(livingSituation)) {
      return List.of(createApplicationInput("TEMPORARILY_WITH_FRIENDS_OR_FAMILY"));
    }

    if ("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING".equals(livingSituation)) {
      return List.of(
          createApplicationInput(livingSituation),
          new ApplicationInput("livingSituation", "county",
              getFirstValue(pagesData, IDENTIFY_COUNTY), ApplicationInputType.SINGLE_VALUE)
      );
    }

    return List.of(createApplicationInput(livingSituation));
  }

  @NotNull
  private ApplicationInput createApplicationInput(String value) {
    return new ApplicationInput("livingSituation", "derivedLivingSituation",
        List.of(value),
        ApplicationInputType.ENUMERATED_SINGLE_VALUE);
  }
}
