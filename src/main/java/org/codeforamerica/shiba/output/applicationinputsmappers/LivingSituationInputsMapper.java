package org.codeforamerica.shiba.output.applicationinputsmappers;

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
      return createApplicationInput("UNKNOWN");
    }

    if (TEMPORARILY_WITH_FRIENDS_OR_FAMILY_OPTIONS.contains(livingSituation)) {
      return createApplicationInput("TEMPORARILY_WITH_FRIENDS_OR_FAMILY");
    }
    return createApplicationInput(livingSituation);
  }

  @NotNull
  private List<ApplicationInput> createApplicationInput(String value) {
    return List.of(new ApplicationInput("livingSituation", "derivedLivingSituation",
        List.of(value),
        ApplicationInputType.ENUMERATED_SINGLE_VALUE));
  }
}
