package org.codeforamerica.shiba.output.caf;

public interface JobIncomeInformation {
    boolean isComplete();

    Double grossMonthlyIncome();

    int getIteration();
}
