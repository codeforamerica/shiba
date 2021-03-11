package org.codeforamerica.shiba.output.caf;

import lombok.Value;
import org.codeforamerica.shiba.pages.data.Iteration;

@Value
public class NonHourlyJobIncomeInformation implements JobIncomeInformation {
    PayPeriod payPeriod;
    Double incomePerPayPeriod;
    int indexInJobsSubworkflow;
    Iteration iteration;

    public NonHourlyJobIncomeInformation(String payPeriod, String incomePerPayPeriod, int indexInJobsSubworkflow, Iteration iteration) {
        this.payPeriod = payPeriod.isEmpty() ? null : PayPeriod.valueOf(payPeriod);
        this.incomePerPayPeriod = incomePerPayPeriod.isEmpty() ? null : Double.valueOf(incomePerPayPeriod.replace(",",""));
        this.indexInJobsSubworkflow = indexInJobsSubworkflow;
        this.iteration = iteration;
    }

    @Override
    public boolean isComplete() {
        return payPeriod != null && incomePerPayPeriod != null;
    }

    @Override
    public Double grossMonthlyIncome() {
        return switch (payPeriod) {
            case EVERY_WEEK -> incomePerPayPeriod * 4;
            case EVERY_TWO_WEEKS, TWICE_A_MONTH -> incomePerPayPeriod * 2;
            case EVERY_MONTH, IT_VARIES -> incomePerPayPeriod;
        };
    }

    enum PayPeriod {
        EVERY_WEEK,
        EVERY_TWO_WEEKS,
        TWICE_A_MONTH,
        EVERY_MONTH,
        IT_VARIES
    }
}
