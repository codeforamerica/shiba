package org.codeforamerica.shiba.output.applicationinputsmappers;

import static java.lang.Boolean.parseBoolean;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IS_SELF_EMPLOYMENT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker.IterationScopeInfo;
import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.PageGroupConfiguration;
import org.springframework.stereotype.Component;

@Component
public class HourlyJobInputsMapper implements ApplicationInputsMapper {

  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;
  private final ApplicationConfiguration applicationConfiguration;

  public HourlyJobInputsMapper(GrossMonthlyIncomeParser grossMonthlyIncomeParser,
      ApplicationConfiguration applicationConfiguration) {
    this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    this.applicationConfiguration = applicationConfiguration;
  }

  @Override
  public List<ApplicationInput> map(Application application, Document document,
      Recipient _recipient, SubworkflowIterationScopeTracker scopeTracker) {
    List<JobIncomeInformation> jobs = grossMonthlyIncomeParser.parse(
        application.getApplicationData());
    PageGroupConfiguration jobGroup = applicationConfiguration.getPageGroups().get("jobs");
    List<ApplicationInput> result = new ArrayList<>();
    for (int i = 0; i < jobs.size(); i++) {
      // ScopeTracker needs to track for every job iteration, even though we are only adding for hourly jobs
      IterationScopeInfo scopeInfo = scopeTracker.getIterationScopeInfo(jobGroup,
          jobs.get(i).getIteration());
      if (jobs.get(i) instanceof HourlyJobIncomeInformation hourlyJob) {
        result.add(new ApplicationInput("payPeriod", "payPeriod", "Hourly",
            SINGLE_VALUE, i));

        // Add non-self-employment scope for certain-pops only
        if (document == Document.CERTAIN_POPS && !parseBoolean(
            getFirstValue(hourlyJob.getIteration().getPagesData(), IS_SELF_EMPLOYMENT))) {
          if (scopeInfo != null) {
            result.add(new ApplicationInput(
                scopeInfo.getScope() + "_payPeriod",
                "payPeriod",
                "Hourly",
                SINGLE_VALUE,
                scopeInfo.getIndex()
            ));
          }
        }
      }
    }

    return result;
  }
}
