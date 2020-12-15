package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.data.Iteration;

public interface JobIncomeInformation {
    boolean isComplete();

    Double grossMonthlyIncome();

    int getIndexInJobsSubworkflow();

    Iteration getIteration();
}
