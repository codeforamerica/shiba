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

@Component
public class GrossMonthlyIncomeParser extends ApplicationDataParser<List<JobIncomeInformation>> {
    public GrossMonthlyIncomeParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    public List<JobIncomeInformation> parse(ApplicationData data) {
        PageInputCoordinates lastThirtyDaysIncomeCoordinates = parsingConfiguration.get("lastThirtyDaysJobIncome");
        Subworkflow jobsGroup = data.getSubworkflows().get(lastThirtyDaysIncomeCoordinates.getGroupName());
        if (jobsGroup == null) {
            return Collections.emptyList();
        }

        return jobsGroup.stream()
                .map(iteration -> {
                    PagesData pagesData = iteration.getPagesData();
                    boolean hasLastThirtyDaysIncome = pagesData.containsKey(lastThirtyDaysIncomeCoordinates.getPageName());
                    if (hasLastThirtyDaysIncome) {
                        String lastThirtyDaysIncome = parseValue("lastThirtyDaysJobIncome", pagesData);
                        return new LastThirtyDaysJobIncomeInformation(lastThirtyDaysIncome, jobsGroup.indexOf(pagesData), iteration);
                    } else {
                        boolean isHourlyJob = Boolean.parseBoolean(parseValue("paidByTheHour", pagesData));
                        if (isHourlyJob) {
                            String hourlyWageInputValue = parseValue("hourlyWage", pagesData);
                            String hoursAWeekInputValue = parseValue("hoursAWeek", pagesData);
                            return new HourlyJobIncomeInformation(hourlyWageInputValue, hoursAWeekInputValue, jobsGroup.indexOf(pagesData), iteration);
                        } else {
                            String payPeriodInputValue = parseValue("payPeriod", pagesData);
                            String incomePerPayPeriodInputValue = parseValue("incomePerPayPeriod", pagesData);
                            return new NonHourlyJobIncomeInformation(payPeriodInputValue, incomePerPayPeriodInputValue, jobsGroup.indexOf(pagesData), iteration);
                        }
                    }
                })
                .filter(JobIncomeInformation::isComplete)
                .collect(Collectors.toList());
    }
}
