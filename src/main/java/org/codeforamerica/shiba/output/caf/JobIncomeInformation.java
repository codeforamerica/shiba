package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.Iteration;

import java.math.BigDecimal;

public interface JobIncomeInformation {
    boolean isComplete();

    Money grossMonthlyIncome();

    int getIndexInJobsSubworkflow();

    Iteration getIteration();

    default BigDecimal parseWithCommasRemoved(String s) {
        try {
            return new BigDecimal(s.replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
