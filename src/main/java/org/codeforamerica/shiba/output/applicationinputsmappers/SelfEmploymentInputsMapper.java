package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.codeforamerica.shiba.output.FullNameFormatter.getFullName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SelfEmploymentInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document document,
      Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {

    List<String> selfEmploymentInputs = getValues(JOBS, IS_SELF_EMPLOYMENT,
        application.getApplicationData());

    if (selfEmploymentInputs == null) {
      return Collections.emptyList();
    }

    if (document == Document.CERTAIN_POPS) {
      // Is applicant self-employed?
      Subworkflow jobs = getGroup(application.getApplicationData(), JOBS);
      boolean hasSelfEmployedJob = Optional.ofNullable(jobs.stream())
          .orElse(Stream.empty())
          .map(Iteration::getPagesData)
          .anyMatch(pagesData -> getFirstValue(pagesData, WHOSE_JOB_IS_IT).contains("applicant")
                                 && getFirstValue(pagesData, IS_SELF_EMPLOYMENT).equals("true"));

      List<ApplicationInput> results = new ArrayList<>();
      if (hasSelfEmployedJob) {
        results.add(createApplicationInput("selfEmployed", "true"));
        results.add(createApplicationInput("selfEmployedApplicantName", getFullName(application)));
      } else {
        results.add(createApplicationInput("selfEmployed", "false"));
      }

      return results;
    } else {
      // Is anyone in the household self-employed?
      boolean hasSelfEmployedJob = selfEmploymentInputs.contains("true");
      return List.of(
          createApplicationInput("selfEmployed", hasSelfEmployedJob ? "true" : "false"),
          createApplicationInput("selfEmployedGrossMonthlyEarnings",
              hasSelfEmployedJob ? "see question 9" : ""));
    }
  }

  @NotNull
  private ApplicationInput createApplicationInput(String name, String value) {
    return new ApplicationInput("employee", name, value, SINGLE_VALUE);
  }
}
