package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import static org.codeforamerica.shiba.output.FullNameFormatter.getFullName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SelfEmploymentPreparer extends SubworkflowScopePreparer {

  @Override
  protected ScopedParams getParams(Document _document, Application application) {
    return new ScopedParams(
        pagesData -> getBooleanValue(pagesData, IS_SELF_EMPLOYMENT),
        JOBS,
        "selfEmployment_");
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient _recipient) {

    List<String> selfEmploymentInputs = getValues(application.getApplicationData(), JOBS, IS_SELF_EMPLOYMENT);

    if (selfEmploymentInputs == null) {
      return Collections.emptyList();
    }

    if (document == Document.CERTAIN_POPS) {
      // Is applicant self-employed?
      Subworkflow jobs = getGroup(application.getApplicationData(), JOBS);
      boolean hasSelfEmployedJob = Optional.ofNullable(jobs.stream())
          .orElse(Stream.empty())
          .map(Iteration::getPagesData)
          .anyMatch(pagesData -> (getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains("applicant") 
        		  || getFirstValue(pagesData, WHOSE_JOB_IS_IT).isEmpty())
              && getFirstValue(pagesData, IS_SELF_EMPLOYMENT).equals("true"));

      List<DocumentField> results = super.prepareDocumentFields(application, document);
      if (hasSelfEmployedJob) {
        results.add(createApplicationInput("selfEmployed", "true"));
        results.add(createApplicationInput("selfEmployedApplicantName", getFullName(application)));
      } else {
        results.add(createApplicationInput("selfEmployed", "false"));
      }

      return results;
    } else {
      List<DocumentField> results = super.prepareDocumentFields(application, document);

      // Is anyone in the household self-employed?
      boolean hasSelfEmployedJob = selfEmploymentInputs.contains("true");
      results.add(createApplicationInput("selfEmployed", hasSelfEmployedJob ? "true" : "false"));
      results.add(createApplicationInput("selfEmployedGrossMonthlyEarnings",
          hasSelfEmployedJob ? "see question 9" : ""));

      return results;
    }
  }

  @NotNull
  private DocumentField createApplicationInput(String name, String value) {
    return new DocumentField("employee", name, value, SINGLE_VALUE);
  }
}
