package org.codeforamerica.shiba.output.caf;

import lombok.Value;

@Value
public class LastThirtyDaysJobIncomeInformation implements JobIncomeInformation {
    Double lastThirtyDaysIncome;
    int iteration;

    public LastThirtyDaysJobIncomeInformation(String lastThirtyDaysIncome, int iteration) {
        this.lastThirtyDaysIncome = lastThirtyDaysIncome.isBlank() ? 0.0 : Double.parseDouble(lastThirtyDaysIncome);
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

    @Override
    public int getIteration() {
        return iteration;
    }

}
