package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLICANT_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.BASIC_CRITERIA_CERTAIN_POPS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HAS_DISABILITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class BasicCriteriaInputsMapper extends OneToManyApplicationInputsMapper {

  public static final List<String> BLIND_OR_HAS_DISABILITY = List.of("BLIND", "SSI_OR_RSDI",
      "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT", "MEDICAL_ASSISTANCE");

  public static final List<String> DETERMINED_DISABILITY = List.of("SSI_OR_RSDI",
      "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT");

  private static final List<String> BASIC_CRITERIA_OPTIONS = List.of("SIXTY_FIVE_OR_OLDER", "BLIND",
      "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT", "MEDICAL_ASSISTANCE",
      "HELP_WITH_MEDICARE");

  @Override
  public List<ApplicationInput> map(Application application, Document _document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams("basicCriteria", BASIC_CRITERIA_CERTAIN_POPS,
        BASIC_CRITERIA_OPTIONS);
  }

  protected List<ApplicationInput> map(PagesData pagesData) {
    List<String> programs = getValues(pagesData, APPLICANT_PROGRAMS);
    if (!programs.contains(Program.CERTAIN_POPS)) {
      // Not applicable
      return Collections.emptyList();
    }

    List<ApplicationInput> result = super.map(pagesData);

    List<String> criteriaSelections = getValues(pagesData, BASIC_CRITERIA_CERTAIN_POPS);

    boolean blindOrHasDisability =
        criteriaSelections.stream().anyMatch(BLIND_OR_HAS_DISABILITY::contains)
            || getBooleanValue(pagesData, HAS_DISABILITY);

    if (blindOrHasDisability) {
      result.add(createApplicationInput(true, "blindOrHasDisability"));

      boolean determinedDisability =
          criteriaSelections.stream().anyMatch(DETERMINED_DISABILITY::contains);
      result.add(createApplicationInput(determinedDisability, "disabilityDetermination"));
    }

    if (criteriaSelections.contains("SSI_OR_RSDI")) {
      result.add(createApplicationInput(true, "SSI_OR_RSDI"));
    }

    return result;
  }

  @NotNull
  private ApplicationInput createApplicationInput(boolean value, String name) {
    return new ApplicationInput("basicCriteria", name,
        String.valueOf(value), ApplicationInputType.ENUMERATED_SINGLE_VALUE);
  }
}
