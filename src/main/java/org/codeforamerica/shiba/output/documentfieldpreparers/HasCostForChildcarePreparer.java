package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.lang.Boolean.parseBoolean;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLICANT_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_GOING_TO_SCHOOL;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_LOOKING_FOR_JOB;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHO_IS_GOING_TO_SCHOOL;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHO_IS_LOOKING_FOR_A_JOB;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.HOUSEHOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;
import static org.codeforamerica.shiba.output.FullNameFormatter.getId;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class HasCostForChildcarePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document _document,
      Recipient _recipient) {
    return map(application);
  }

  private List<DocumentField> map(Application application) {
    ApplicationData applicationData = application.getApplicationData();
    PagesData pagesData = applicationData.getPagesData();

    Set<String> relevantIds = new HashSet<>();
    getValues(pagesData, WHO_IS_GOING_TO_SCHOOL)
        .forEach(nameAndId -> relevantIds.add(getId(nameAndId)));
    getValues(pagesData, WHO_IS_LOOKING_FOR_A_JOB)
        .forEach(nameAndId -> relevantIds.add(getId(nameAndId)));

    // Applicant won't have their own iteration in subworkflow
    boolean applicantHasCCAP = getValues(pagesData, APPLICANT_PROGRAMS).contains(CCAP);
    if (applicantHasCCAP && relevantIds.contains("applicant")) {
      return createApplicationInput();
    }

    Subworkflow subworkflow = getGroup(applicationData, HOUSEHOLD);
    if (subworkflow != null) {
      // Check if anyone else in the household applied for CCAP and is either going to school or looking for a job
      for (Iteration iteration : subworkflow) {
        boolean hasCCAP = getValues(iteration.getPagesData(), HOUSEHOLD_PROGRAMS).contains(CCAP);
        String id = iteration.getId().toString();
        if (hasCCAP && relevantIds.contains(id)) {
          return createApplicationInput();
        }
      }
    } else {
      // Applicant lives alone
      boolean goingToSchool = parseBoolean(getFirstValue(pagesData, IS_GOING_TO_SCHOOL));
      boolean lookingForJob = parseBoolean(getFirstValue(pagesData, IS_LOOKING_FOR_JOB));
      if (applicantHasCCAP && (goingToSchool || lookingForJob)) {
        return createApplicationInput();
      }
    }

    return Collections.emptyList();
  }

  @NotNull
  private List<DocumentField> createApplicationInput() {
    return List.of(
        new DocumentField("ccapHasCostsForChildCare",
            "ccapHasCostsForChildCare",
            "true",
            ENUMERATED_SINGLE_VALUE));
  }
}
