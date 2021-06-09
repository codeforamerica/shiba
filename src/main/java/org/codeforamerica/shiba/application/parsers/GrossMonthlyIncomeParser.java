package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.LastThirtyDaysJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.NonHourlyJobIncomeInformation;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.Field.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.getFirstValue;

@Component
public class GrossMonthlyIncomeParser {

    public List<JobIncomeInformation> parse(ApplicationData data) {
        Subworkflow jobsGroup = data.getSubworkflows().get("jobs");
        if (jobsGroup == null) {
            return Collections.emptyList();
        }

        return jobsGroup.stream()
                .map(iteration -> {
                    PagesData pagesData = iteration.getPagesData();
                    String lastThirtyDaysIncome = getFirstValue(pagesData, LAST_THIRTY_DAYS_JOB_INCOME);
                    if (lastThirtyDaysIncome != null) {
                        return new LastThirtyDaysJobIncomeInformation(lastThirtyDaysIncome, jobsGroup.indexOf(pagesData), iteration);
                    } else {
                        boolean isHourlyJob = Boolean.parseBoolean(getFirstValue(pagesData, PAID_BY_THE_HOUR));
                        if (isHourlyJob) {
                            String hourlyWageInputValue = getFirstValue(pagesData, HOURLY_WAGE);
                            String hoursAWeekInputValue = getFirstValue(pagesData, HOURS_A_WEEK);
                            return new HourlyJobIncomeInformation(hourlyWageInputValue, hoursAWeekInputValue, jobsGroup.indexOf(pagesData), iteration);
                        } else {
                            String payPeriodInputValue = getFirstValue(pagesData, PAY_PERIOD);
                            String incomePerPayPeriodInputValue = getFirstValue(pagesData, INCOME_PER_PAY_PERIOD);
                            return new NonHourlyJobIncomeInformation(payPeriodInputValue, incomePerPayPeriodInputValue, jobsGroup.indexOf(pagesData), iteration);
                        }
                    }
                })
                .filter(JobIncomeInformation::isComplete)
                .collect(Collectors.toList());
    }
}
