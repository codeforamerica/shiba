package org.codeforamerica.shiba.output.caf;

import lombok.Value;
import org.codeforamerica.shiba.Money;

import java.util.List;

@Value
public class SnapExpeditedEligibilityParameters {
    Money assets;
    Money last30DaysIncome;
    List<JobIncomeInformation> jobIncomeInformation;
    boolean isMigrantWorker;
    Money housingCosts;
    List<String> utilityExpenses;
    boolean applyingForSnap;
}
