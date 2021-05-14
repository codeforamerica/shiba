package org.codeforamerica.shiba.output.caf;

import lombok.Value;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.Iteration;

@Value
public class HourlyJobIncomeInformation implements JobIncomeInformation {
    Integer hourlyWage; // could be optionalint! but IDE insprection said that's bad
    Integer hoursAWeek;
    int indexInJobsSubworkflow;
    Iteration iteration;

    // NaN

    public HourlyJobIncomeInformation(String hourlyWage, String hoursAWeek, int indexInJobsSubworkflow, Iteration iteration) {
        int parsedWage = Money.parse(hourlyWage).orElse(-1);
        this.hourlyWage = parsedWage < 0 ? null : parsedWage;
        this.hoursAWeek = hoursAWeek.isEmpty() ? null : Integer.parseInt(hoursAWeek);
        this.indexInJobsSubworkflow = indexInJobsSubworkflow;
        this.iteration = iteration;
    }

    @Override
    public boolean isComplete() {
        return hourlyWage != null && hoursAWeek != null;
    }

    @Override
    public Integer grossMonthlyIncome() {
        return hourlyWage * hoursAWeek * 4;
    }
}
