package org.codeforamerica.shiba.output.caf;

import lombok.Value;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.Iteration;

@Value
public class LastThirtyDaysJobIncomeInformation implements JobIncomeInformation {
    Integer lastThirtyDaysIncome;
    int indexInJobsSubworkflow;
    Iteration iteration;

    public LastThirtyDaysJobIncomeInformation(String lastThirtyDaysIncome, int indexInJobsSubworkflow, Iteration iteration) {
        this.lastThirtyDaysIncome = lastThirtyDaysIncome.isBlank() ? 0 : Money.parse(lastThirtyDaysIncome).orElseThrow(IllegalArgumentException::new);
        this.indexInJobsSubworkflow = indexInJobsSubworkflow;
        this.iteration = iteration;
    }

    @Override
    public boolean isComplete() {
        return lastThirtyDaysIncome != null;
    }

    @Override
    public Integer grossMonthlyIncome() {
        return lastThirtyDaysIncome;
    }
}
