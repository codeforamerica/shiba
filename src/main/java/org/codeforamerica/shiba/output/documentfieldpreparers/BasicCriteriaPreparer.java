package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLICANT_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.BASIC_CRITERIA_CERTAIN_POPS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HAS_DISABILITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class BasicCriteriaPreparer extends OneToManyDocumentFieldPreparer {

  public static final List<String> BLIND_OR_HAS_DISABILITY = List.of("BLIND", "SSI_OR_RSDI",
      "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT", "MEDICAL_ASSISTANCE");

  public static final List<String> DETERMINED_DISABILITY = List.of("SSI_OR_RSDI",
      "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT");

  private static final List<String> BASIC_CRITERIA_OPTIONS = List.of("SIXTY_FIVE_OR_OLDER", "BLIND",
      "HAVE_DISABILITY_SSA", "HAVE_DISABILITY_SMRT", "MEDICAL_ASSISTANCE",
      "SSI_OR_RSDI");

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document _document,
      Recipient _recipient) {
    return map(application.getApplicationData().getPagesData());
  }

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams("basicCriteria", BASIC_CRITERIA_CERTAIN_POPS,
        BASIC_CRITERIA_OPTIONS);
  }

  protected List<DocumentField> map(PagesData pagesData) {
    List<String> programs = getValues(pagesData, APPLICANT_PROGRAMS);
    if (!programs.contains(Program.CERTAIN_POPS)) {
      // Not applicable
      return Collections.emptyList();
    }

    List<DocumentField> result = super.map(pagesData);

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

    if (criteriaSelections.contains("HELP_WITH_MEDICARE")) {
      result.add(createApplicationInput(true, "HELP_WITH_MEDICARE"));
    }

    return result;
  }

  @NotNull
  private DocumentField createApplicationInput(boolean value, String name) {
    return new DocumentField("basicCriteria", name,
        String.valueOf(value), DocumentFieldType.ENUMERATED_SINGLE_VALUE);
  }
}
