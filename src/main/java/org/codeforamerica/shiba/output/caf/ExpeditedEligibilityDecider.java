package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.parsers.ExpeditedEligibilityParser;
import org.codeforamerica.shiba.output.TotalIncome;
import org.codeforamerica.shiba.output.TotalIncomeCalculator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.output.caf.ExpeditedEligibility.UNDETERMINED;

@Component
public class ExpeditedEligibilityDecider {
    private final UtilityDeductionCalculator utilityDeductionCalculator;
    private final TotalIncomeCalculator totalIncomeCalculator;
    private final ExpeditedEligibilityParser expeditedEligibilityParser;
    public static final int ASSET_THRESHOLD = 100;
    public static final int INCOME_THRESHOLD = 150;

    public ExpeditedEligibilityDecider(UtilityDeductionCalculator utilityDeductionCalculator,
                                       TotalIncomeCalculator totalIncomeCalculator,
                                       ExpeditedEligibilityParser expeditedEligibilityParser) {
        this.utilityDeductionCalculator = utilityDeductionCalculator;
        this.totalIncomeCalculator = totalIncomeCalculator;
        this.expeditedEligibilityParser = expeditedEligibilityParser;
    }

    public ExpeditedEligibility decide(ApplicationData applicationData) {
        return expeditedEligibilityParser.parse(applicationData)
                .map(parameters -> {
                            double assets = parameters.getAssets();
                            double income = totalIncomeCalculator.calculate(new TotalIncome(parameters.getLastMonthsIncome(), parameters.getJobIncomeInformation()));
                            double housingCosts = parameters.getHousingCosts();

                            boolean assetsAndIncomeBelowThreshold = assets <= ASSET_THRESHOLD && income < INCOME_THRESHOLD;
                            boolean migrantWorkerAndAssetsBelowThreshold = parameters.isMigrantWorker() && assets <= ASSET_THRESHOLD;
                            int standardDeduction = utilityDeductionCalculator.calculate(parameters.getUtilityExpenses());

                            return assetsAndIncomeBelowThreshold
                                    || migrantWorkerAndAssetsBelowThreshold
                                    || (assets + income) < (housingCosts + standardDeduction) ? ExpeditedEligibility.ELIGIBLE : ExpeditedEligibility.NOT_ELIGIBLE;
                        }
                ).orElse(UNDETERMINED);
    }
}
