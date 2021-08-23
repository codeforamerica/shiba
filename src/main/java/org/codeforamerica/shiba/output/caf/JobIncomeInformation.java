package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.Iteration;

public interface JobIncomeInformation {

  boolean isComplete();

  Money grossMonthlyIncome();

  int getIndexInJobsSubworkflow();

  Iteration getIteration();
}
