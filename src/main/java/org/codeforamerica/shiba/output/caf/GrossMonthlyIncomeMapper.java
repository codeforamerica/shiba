package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class GrossMonthlyIncomeMapper implements ApplicationInputsMapper {

    private final GrossMonthlyIncomeConfiguration grossMonthlyIncomeConfiguration;

    public GrossMonthlyIncomeMapper(GrossMonthlyIncomeConfiguration grossMonthlyIncomeConfiguration) {
        this.grossMonthlyIncomeConfiguration = grossMonthlyIncomeConfiguration;
    }

    @Override
    public List<ApplicationInput> map(Application application) {
        ApplicationData data = application.getApplicationData();
        Subworkflow jobsGroup = data.getSubworkflows().get(grossMonthlyIncomeConfiguration.getGroupName());
        if (jobsGroup == null) {
            return Collections.emptyList();
        }

        return jobsGroup.stream()
                .map(pagesData -> {
                    PageInputCoordinates isHourlyJobCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("paidByTheHour");
                    boolean isHourlyJob = Boolean.parseBoolean(pagesData.getPage(isHourlyJobCoordinates.getPageName()).get(isHourlyJobCoordinates.getInputName()).getValue().get(0));
                    if (isHourlyJob) {
                        PageInputCoordinates hourlyWageCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("hourlyWage");
                        String hourlyWageInputValue = pagesData.getPage(hourlyWageCoordinates.getPageName())
                                .get(hourlyWageCoordinates.getInputName()).getValue().get(0);
                        PageInputCoordinates hoursAWeekCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("hoursAWeek");
                        String hoursAWeekInputValue = pagesData.getPage(hoursAWeekCoordinates.getPageName())
                                .get(hoursAWeekCoordinates.getInputName()).getValue().get(0);
                        return new HourlyJobIncomeInformation(hourlyWageInputValue, hoursAWeekInputValue, jobsGroup.indexOf(pagesData));
                    } else {
                        PageInputCoordinates payPeriodCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("payPeriod");
                        String payPeriodInputValue = pagesData.getPage(payPeriodCoordinates.getPageName())
                                .get(payPeriodCoordinates.getInputName()).getValue().get(0);
                        PageInputCoordinates incomePerPayPeriodCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("incomePerPayPeriod");
                        String incomePerPayPeriodInputValue = pagesData.getPage(incomePerPayPeriodCoordinates.getPageName())
                                .get(incomePerPayPeriodCoordinates.getInputName()).getValue().get(0);
                        return new NonHourlyJobIncomeInformation(payPeriodInputValue, incomePerPayPeriodInputValue, jobsGroup.indexOf(pagesData));
                    }
                })
                .filter(JobIncomeInformation::isComplete)
                .map(jobIncomeInformation ->
                        new ApplicationInput(
                                "employee",
                                "grossMonthlyIncome",
                                List.of(String.valueOf(jobIncomeInformation.grossMonthlyIncome())),
                                SINGLE_VALUE,
                                jobIncomeInformation.getIteration()))
                .collect(Collectors.toList());
    }

}
