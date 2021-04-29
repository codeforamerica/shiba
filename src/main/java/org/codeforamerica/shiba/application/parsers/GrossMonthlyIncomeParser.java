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
        ParsingCoordinates grossMonthlyIncomeConfiguration = parsingConfiguration.get("grossMonthlyIncome");
        Subworkflow jobsGroup = data.getSubworkflows().get(grossMonthlyIncomeConfiguration.getGroupName());
        if (jobsGroup == null) {
            return Collections.emptyList();
        }

        return jobsGroup.stream()
                .map(iteration -> {
                    PagesData pagesData = iteration.getPagesData();
                    PageInputCoordinates isHourlyJobCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("paidByTheHour");
                    PageInputCoordinates lastThirtyDaysIncomeCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("lastThirtyDaysJobIncome");
                    boolean hasLastThirtyDaysIncome = pagesData.containsKey(lastThirtyDaysIncomeCoordinates.getPageName());
                    if (hasLastThirtyDaysIncome) {
                        String lastThirtyDaysIncome = pagesData.getPage(lastThirtyDaysIncomeCoordinates.getPageName()).get(lastThirtyDaysIncomeCoordinates.getInputName()).getValue(0);
                        return new LastThirtyDaysJobIncomeInformation(lastThirtyDaysIncome, jobsGroup.indexOf(pagesData), iteration);
                    } else {
                        boolean isHourlyJob = Boolean.parseBoolean(pagesData.getPageInputFirstValue(isHourlyJobCoordinates.getPageName(), isHourlyJobCoordinates.getInputName()));
                        if (isHourlyJob) {
                            PageInputCoordinates hourlyWageCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("hourlyWage");
                            String hourlyWageInputValue = pagesData.getPage(hourlyWageCoordinates.getPageName())
                                    .get(hourlyWageCoordinates.getInputName()).getValue(0);
                            PageInputCoordinates hoursAWeekCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("hoursAWeek");
                            String hoursAWeekInputValue = pagesData.getPage(hoursAWeekCoordinates.getPageName())
                                    .get(hoursAWeekCoordinates.getInputName()).getValue(0);
                            return new HourlyJobIncomeInformation(hourlyWageInputValue, hoursAWeekInputValue, jobsGroup.indexOf(pagesData), iteration);
                        } else {
                            PageInputCoordinates payPeriodCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("payPeriod");
                            String payPeriodInputValue = pagesData.getPage(payPeriodCoordinates.getPageName())
                                    .get(payPeriodCoordinates.getInputName()).getValue(0);
                            PageInputCoordinates incomePerPayPeriodCoordinates = grossMonthlyIncomeConfiguration.getPageInputs().get("incomePerPayPeriod");
                            String incomePerPayPeriodInputValue = pagesData.getPage(incomePerPayPeriodCoordinates.getPageName())
                                    .get(incomePerPayPeriodCoordinates.getInputName()).getValue(0);
                            return new NonHourlyJobIncomeInformation(payPeriodInputValue, incomePerPayPeriodInputValue, jobsGroup.indexOf(pagesData), iteration);
                        }
                    }
                })
                .filter(JobIncomeInformation::isComplete)
                .collect(Collectors.toList());
    }
}
