package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.parsers.SnapExpeditedEligibilityParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.UNDETERMINED;

@Component
public class SnapExpeditedEligibilityDecider {
    private final UtilityDeductionCalculator utilityDeductionCalculator;
    private final TotalIncomeCalculator totalIncomeCalculator;
    private final SnapExpeditedEligibilityParser snapExpeditedEligibilityParser;
    public static final int ASSET_THRESHOLD = 100;
    public static final int INCOME_THRESHOLD = 150;

    public SnapExpeditedEligibilityDecider(UtilityDeductionCalculator utilityDeductionCalculator,
                                           TotalIncomeCalculator totalIncomeCalculator,
                                           SnapExpeditedEligibilityParser snapExpeditedEligibilityParser) {
        this.utilityDeductionCalculator = utilityDeductionCalculator;
        this.totalIncomeCalculator = totalIncomeCalculator;
        this.snapExpeditedEligibilityParser = snapExpeditedEligibilityParser;
    }

    public SnapExpeditedEligibility decide(ApplicationData applicationData) {
        return snapExpeditedEligibilityParser.parse(applicationData)
                .map(parameters -> {
                            double assets = parameters.getAssets();
                            double income = totalIncomeCalculator.calculate(new TotalIncome(parameters.getLast30DaysIncome(), parameters.getJobIncomeInformation()));
                            double housingCosts = parameters.getHousingCosts();

                            boolean assetsAndIncomeBelowThreshold = assets <= ASSET_THRESHOLD && income < INCOME_THRESHOLD;
                            boolean migrantWorkerAndAssetsBelowThreshold = parameters.isMigrantWorker() && assets <= ASSET_THRESHOLD;
                            int standardDeduction = utilityDeductionCalculator.calculate(parameters.getUtilityExpenses());

                            return assetsAndIncomeBelowThreshold
                                    || migrantWorkerAndAssetsBelowThreshold
                                    || (assets + income) < (housingCosts + standardDeduction) ? SnapExpeditedEligibility.ELIGIBLE : SnapExpeditedEligibility.NOT_ELIGIBLE;
                        }
                ).orElse(UNDETERMINED);
    }
}
