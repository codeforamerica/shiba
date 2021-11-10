package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.lang.Boolean.parseBoolean;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.springframework.stereotype.Component;

@Component
public class HourlyJobPreparer implements DocumentFieldPreparer {

  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

  public HourlyJobPreparer(GrossMonthlyIncomeParser grossMonthlyIncomeParser) {
    this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient _recipient, SubworkflowIterationScopeTracker scopeTracker) {
    List<JobIncomeInformation> jobs =
        grossMonthlyIncomeParser.parse(application.getApplicationData());

    int nonSelfEmploymentIndex = 0;
    List<DocumentField> result = new ArrayList<>();
    for (int i = 0; i < jobs.size(); i++) {
      boolean isNonSelfEmployment = !parseBoolean(
          getFirstValue(jobs.get(i).getIteration().getPagesData(), IS_SELF_EMPLOYMENT));
      // ScopeTracker needs to track for every job iteration, even though we are only adding for hourly jobs
      if (jobs.get(i) instanceof HourlyJobIncomeInformation hourlyJob) {
        result.add(new DocumentField("payPeriod", "payPeriod", "Hourly",
            SINGLE_VALUE, i));

        // Add non-self-employment scope for certain-pops only
        if (document == Document.CERTAIN_POPS && isNonSelfEmployment) {
          result.add(new DocumentField(
              "nonSelfEmployment_payPeriod",
              "payPeriod",
              "Hourly",
              SINGLE_VALUE,
              nonSelfEmploymentIndex
          ));
        }
      }
      if (isNonSelfEmployment) {
        nonSelfEmploymentIndex++;
      }
    }

    return result;
  }
}
