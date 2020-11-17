package org.codeforamerica.shiba.output.caf;

import lombok.Value;

import java.util.List;

@Value
public class ExpeditedEligibilityParameters {
    double assets;
    double last30DaysIncome;
    List<JobIncomeInformation> jobIncomeInformation;
    boolean isMigrantWorker;
    double housingCosts;
    List<String> utilityExpenses;
}
