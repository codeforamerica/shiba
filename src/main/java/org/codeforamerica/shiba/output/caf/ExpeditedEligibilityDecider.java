package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ExpeditedEligibilityDecider {
    private final UtilityDeductionCalculator utilityDeductionCalculator;

    public ExpeditedEligibilityDecider(UtilityDeductionCalculator utilityDeductionCalculator) {
        this.utilityDeductionCalculator = utilityDeductionCalculator;
    }

    public boolean decide(Map<String, InputData> inputMap) {
        int assetThreshold = 100;
        int incomeThreshold = 150;

        if (!inputMap.keySet().containsAll(List.of(
                "liquidAssets_liquidAssets",
                "expeditedIncome_moneyMadeLast30Days",
                "expeditedMigrantFarmWorker_migrantOrSeasonalFarmWorker",
                "expeditedExpensesAmount_expeditedExpensesAmount",
                "expeditedUtilityPayments_payForUtilities"
        ))) {
            return false;
        }

        double assets = getDouble(inputMap, "liquidAssets_liquidAssets");
        double income = getDouble(inputMap, "expeditedIncome_moneyMadeLast30Days");
        String isMigrantWorker = inputMap.get("expeditedMigrantFarmWorker_migrantOrSeasonalFarmWorker").getValue().get(0);
        boolean assetsAndIncomeBelowThreshold = assets <= assetThreshold && income < incomeThreshold;
        boolean migrantWorkerAndAssetsUnderThreshold = isMigrantWorker.equals("true") && assets <= assetThreshold;

        double housingCosts = getDouble(inputMap, "expeditedExpensesAmount_expeditedExpensesAmount");

        InputData utilityExpensesSelections = inputMap.get("expeditedUtilityPayments_payForUtilities");
        int standardDeduction = utilityDeductionCalculator.calculate(utilityExpensesSelections.getValue());

        boolean totalAssetsLessThanCostsPlusDeductions = (assets + income) < (housingCosts + standardDeduction);
        return assetsAndIncomeBelowThreshold
                || migrantWorkerAndAssetsUnderThreshold
                || totalAssetsLessThanCostsPlusDeductions;
    }

    private static double getDouble(Map<String, InputData> inputMap, String input) {
        try {
            return Double.parseDouble(inputMap.get(input).getValue().get(0));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
