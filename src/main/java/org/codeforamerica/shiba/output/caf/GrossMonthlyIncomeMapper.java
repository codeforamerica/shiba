package org.codeforamerica.shiba.output.caf;

import lombok.Value;
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
    public List<ApplicationInput> map(ApplicationData data) {
        Subworkflow jobsGroup = data.getSubworkflows().get(grossMonthlyIncomeConfiguration.getGroupName());
        if(jobsGroup == null) {
            return Collections.emptyList();
        }

        return jobsGroup.stream()
                .filter(pagesData -> {
                    PageInputCoordinates isHourlyJobCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("paidByTheHour");
                    return Boolean.parseBoolean(pagesData.getPage(isHourlyJobCoordinates.getPageName()).get(isHourlyJobCoordinates.getInputName()).getValue().get(0));
                })
                .map(pagesData -> {
                    PageInputCoordinates hourlyWageCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("hourlyWage");
                    String hourlyWageInputValue = pagesData.getPage(hourlyWageCoordinates.getPageName())
                            .get(hourlyWageCoordinates.getInputName()).getValue().get(0);
                    PageInputCoordinates hoursAWeekCoordinates = grossMonthlyIncomeConfiguration.pageInputs.get("hoursAWeek");
                    String hoursAWeekInputValue = pagesData.getPage(hoursAWeekCoordinates.getPageName())
                            .get(hoursAWeekCoordinates.getInputName()).getValue().get(0);
                    return new HourlyJobIncomeInformation(hourlyWageInputValue, hoursAWeekInputValue, jobsGroup.indexOf(pagesData));
                })
                .filter(HourlyJobIncomeInformation::isComplete)
                .map(hourlyJobIncomeInformation -> {
                    int hourlyWage = Integer.parseInt(hourlyJobIncomeInformation.getHourlyWage());
                    int hoursAWeek = Integer.parseInt(hourlyJobIncomeInformation.getHoursAWeek());
                    return new ApplicationInput(
                            "employee",
                            "grossMonthlyIncome",
                            List.of(String.valueOf(hourlyWage * hoursAWeek * 4)),
                            SINGLE_VALUE,
                            hourlyJobIncomeInformation.getIteration());
                }).collect(Collectors.toList());
    }

    @Value
    static class HourlyJobIncomeInformation {
        String hourlyWage;
        String hoursAWeek;
        int iteration;

        boolean isComplete() {
            return !hourlyWage.isEmpty() && !hoursAWeek.isEmpty();
        }
    }
}
