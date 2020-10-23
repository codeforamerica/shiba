package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.caf.*;
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
        ParsingCoordinates grossMonthlyIncomeConfiguration = parsingConfiguration.get("grossMonthlyIncome");
        Subworkflow jobsGroup = data.getSubworkflows().get(grossMonthlyIncomeConfiguration.getGroupName());
        if (jobsGroup == null) {
            return Collections.emptyList();
        }

        return jobsGroup.stream()
                .map(iteration -> {
                    PagesData pagesData = iteration.getPagesData();
                    PageInputCoordinates isHourlyJobCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("paidByTheHour");
                    boolean isHourlyJob = Boolean.parseBoolean(pagesData.getPage(isHourlyJobCoordinates.getPageName()).get(isHourlyJobCoordinates.getInputName()).getValue(0));
                    if (isHourlyJob) {
                        PageInputCoordinates hourlyWageCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("hourlyWage");
                        String hourlyWageInputValue = pagesData.getPage(hourlyWageCoordinates.getPageName())
                                .get(hourlyWageCoordinates.getInputName()).getValue(0);
                        PageInputCoordinates hoursAWeekCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("hoursAWeek");
                        String hoursAWeekInputValue = pagesData.getPage(hoursAWeekCoordinates.getPageName())
                                .get(hoursAWeekCoordinates.getInputName()).getValue(0);
                        return new HourlyJobIncomeInformation(hourlyWageInputValue, hoursAWeekInputValue, jobsGroup.indexOf(pagesData));
                    } else {
                        PageInputCoordinates payPeriodCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("payPeriod");
                        String payPeriodInputValue = pagesData.getPage(payPeriodCoordinates.getPageName())
                                .get(payPeriodCoordinates.getInputName()).getValue(0);
                        PageInputCoordinates incomePerPayPeriodCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("incomePerPayPeriod");
                        String incomePerPayPeriodInputValue = pagesData.getPage(incomePerPayPeriodCoordinates.getPageName())
                                .get(incomePerPayPeriodCoordinates.getInputName()).getValue(0);
                        return new NonHourlyJobIncomeInformation(payPeriodInputValue, incomePerPayPeriodInputValue, jobsGroup.indexOf(pagesData));
                    }
                })
                .filter(JobIncomeInformation::isComplete)
                .collect(Collectors.toList());
    }
}
