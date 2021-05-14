package org.codeforamerica.shiba.output.caf;

import lombok.Value;

import java.util.List;

@Value
public class SnapExpeditedEligibilityParameters {
    int assets;
    int last30DaysIncome;
    List<JobIncomeInformation> jobIncomeInformation;
    boolean isMigrantWorker;
    int housingCosts;
    List<String> utilityExpenses;
    boolean applyingForSnap;
}
