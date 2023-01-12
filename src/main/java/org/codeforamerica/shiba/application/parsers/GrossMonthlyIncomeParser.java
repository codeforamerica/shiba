package org.codeforamerica.shiba.application.parsers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOURLY_WAGE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOURS_A_WEEK;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.INCOME_PER_PAY_PERIOD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.LAST_THIRTY_DAYS_JOB_INCOME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PAID_BY_THE_HOUR;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PAY_PERIOD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.LastThirtyDaysJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.NonHourlyJobIncomeInformation;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

@Component
public class GrossMonthlyIncomeParser {

  public List<JobIncomeInformation> parse(ApplicationData data) {
    Subworkflow jobsGroup = getGroup(data, Group.JOBS);
    if (jobsGroup == null) {
      return Collections.emptyList();
    }

    return jobsGroup.stream()
        .map(iteration -> {
        	return parse(jobsGroup, iteration);
        })
        .filter(JobIncomeInformation::isComplete)
        .collect(Collectors.toList());
  }
  
  // This version of parse will only parse one job.
  //There are different ways that a job is paid:
  // 1. Last 30 days income
  // 2. Hourly
  // 3. Pay period
  public JobIncomeInformation parse(Subworkflow jobsGroup, Iteration iteration) {
      PagesData pagesData = iteration.getPagesData();
      String lastThirtyDaysIncome = getFirstValue(pagesData, LAST_THIRTY_DAYS_JOB_INCOME);
      if (lastThirtyDaysIncome != null) {
        return new LastThirtyDaysJobIncomeInformation(lastThirtyDaysIncome,
            jobsGroup.indexOf(pagesData), iteration);
      } else {
        boolean isHourlyJob = Boolean.parseBoolean(getFirstValue(pagesData, PAID_BY_THE_HOUR));
        if (isHourlyJob) {
          String hourlyWageInputValue = getFirstValue(pagesData, HOURLY_WAGE);
          String hoursAWeekInputValue = getFirstValue(pagesData, HOURS_A_WEEK);
          return new HourlyJobIncomeInformation(hourlyWageInputValue, hoursAWeekInputValue,
              jobsGroup.indexOf(pagesData), iteration);
        } else {
          String payPeriodInputValue = getFirstValue(pagesData, PAY_PERIOD);
          String incomePerPayPeriodInputValue = getFirstValue(pagesData, INCOME_PER_PAY_PERIOD);
          return new NonHourlyJobIncomeInformation(payPeriodInputValue,
              incomePerPayPeriodInputValue, jobsGroup.indexOf(pagesData), iteration);
        }
      }

  }

}
