package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.parsers.SnapExpeditedEligibilityParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.UNDETERMINED;

@Component
public class SnapExpeditedEligibilityDecider {
    private final UtilityDeductionCalculator utilityDeductionCalculator;
    private final TotalIncomeCalculator totalIncomeCalculator;
    private final SnapExpeditedEligibilityParser snapExpeditedEligibilityParser;
    public static final Money ASSET_THRESHOLD = Money.parse("100");
    public static final Money INCOME_THRESHOLD = Money.parse("150");


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
                            // required
                            if (parameters.getMigrantWorker() == null || parameters.getUtilityExpenses() == null) {
                                return UNDETERMINED;
                            }

                            List<String> thirtyDayEstimates = parameters.getThirtyDayEstimates();
                            if (thirtyDayEstimates != null && thirtyDayEstimates.stream().allMatch(String::isBlank)) {
                                return UNDETERMINED;
                            }

                            Money assets = parameters.getAssets();
                            Money income = totalIncomeCalculator.calculate(new TotalIncome(parameters.getLast30DaysIncome(), parameters.getJobIncomeInformation()));
                            Money housingCosts = parameters.getHousingCosts();

                            boolean assetsAndIncomeBelowThreshold =
                                    assets.lessOrEqualTo(ASSET_THRESHOLD) && income.lessThan(INCOME_THRESHOLD);
                            boolean migrantWorkerAndAssetsBelowThreshold = Boolean.parseBoolean(parameters.getMigrantWorker()) && assets.lessOrEqualTo(ASSET_THRESHOLD);
                            Money standardDeduction = utilityDeductionCalculator.calculate(parameters.getUtilityExpenses());

                            boolean passesAssetTest = assets.add(income).lessThan(housingCosts.add(standardDeduction));

                            boolean applyingForSnap = parameters.isApplicantApplyingForSnap() || parameters.isHouseHoldApplyingForSnap() && parameters.isPreparingMealsTogether();

                            return (applyingForSnap &&
                                    (assetsAndIncomeBelowThreshold || migrantWorkerAndAssetsBelowThreshold || passesAssetTest))
                                    ? SnapExpeditedEligibility.ELIGIBLE : SnapExpeditedEligibility.NOT_ELIGIBLE;
                        }
                ).orElse(UNDETERMINED);
    }
}
