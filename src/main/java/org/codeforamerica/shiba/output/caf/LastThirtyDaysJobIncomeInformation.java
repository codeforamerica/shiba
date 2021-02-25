package org.codeforamerica.shiba.output.caf;

import lombok.Value;
import org.codeforamerica.shiba.pages.data.Iteration;

@Value
public class LastThirtyDaysJobIncomeInformation implements JobIncomeInformation {
    Double lastThirtyDaysIncome;
    int indexInJobsSubworkflow;
    Iteration iteration;

    public LastThirtyDaysJobIncomeInformation(String lastThirtyDaysIncome, int indexInJobsSubworkflow, Iteration iteration) {
        this.lastThirtyDaysIncome = lastThirtyDaysIncome.isBlank() ? 0.0 : Double.parseDouble(lastThirtyDaysIncome.replace(",",""));
        this.indexInJobsSubworkflow = indexInJobsSubworkflow;
        this.iteration = iteration;
    }

    @Override
    public boolean isComplete() {
        return lastThirtyDaysIncome != null;
    }

    @Override
    public Double grossMonthlyIncome() {
        return lastThirtyDaysIncome;
    }
}
