package org.codeforamerica.shiba.output.caf;

import lombok.Value;
import org.codeforamerica.shiba.pages.data.Iteration;

@Value
public class HourlyJobIncomeInformation implements JobIncomeInformation {
    Double hourlyWage;
    Double hoursAWeek;
    int indexInJobsSubworkflow;
    Iteration iteration;

    public HourlyJobIncomeInformation(String hourlyWage, String hoursAWeek, int indexInJobsSubworkflow, Iteration iteration) {
        this.hourlyWage = hourlyWage.isEmpty() ? null : Double.valueOf(hourlyWage);
        this.hoursAWeek = hoursAWeek.isEmpty() ? null : Double.valueOf(hoursAWeek);
        this.indexInJobsSubworkflow = indexInJobsSubworkflow;
        this.iteration = iteration;
    }

    @Override
    public boolean isComplete() {
        return hourlyWage != null && hoursAWeek != null;
    }

    @Override
    public Double grossMonthlyIncome() {
        return hourlyWage * hoursAWeek * 4;
    }
}
