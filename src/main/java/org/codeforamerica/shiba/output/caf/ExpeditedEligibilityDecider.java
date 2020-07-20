package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.InputData;
import org.codeforamerica.shiba.pages.PagesData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ExpeditedEligibilityDecider {
    private final UtilityDeductionCalculator utilityDeductionCalculator;
    private final Map<String, PageInputCoordinates> pageInputCoordinatesMap;
    public static final int ASSET_THRESHOLD = 100;
    public static final int INCOME_THRESHOLD = 150;

    public ExpeditedEligibilityDecider(UtilityDeductionCalculator utilityDeductionCalculator,
                                       Map<String, PageInputCoordinates> pageInputCoordinatesMap) {
        this.utilityDeductionCalculator = utilityDeductionCalculator;
        this.pageInputCoordinatesMap = pageInputCoordinatesMap;
    }

    public boolean decide(PagesData pagesData) {
        List<String> requiredPages = pageInputCoordinatesMap.values().stream()
                .filter(coordinates -> coordinates.getDefaultValue() == null)
                .map(PageInputCoordinates::getPageName)
                .collect(Collectors.toList());
        if (!pagesData.keySet().containsAll(requiredPages)) {
            return false;
        }

        double assets = getDouble(pagesData, pageInputCoordinatesMap.get("assets"));
        double income = getDouble(pagesData, pageInputCoordinatesMap.get("income"));
        double housingCosts = getDouble(pagesData, pageInputCoordinatesMap.get("housingCosts"));
        PageInputCoordinates migrantWorkerCoordinates = pageInputCoordinatesMap.get("migrantWorker");
        String isMigrantWorker = pagesData.getPage(migrantWorkerCoordinates.getPageName()).get(migrantWorkerCoordinates.getInputName()).getValue().get(0);
        PageInputCoordinates utilityExpensesSelectionsCoordinates = pageInputCoordinatesMap.get("utilityExpensesSelections");
        InputData utilityExpensesSelections = pagesData.getPage(utilityExpensesSelectionsCoordinates.getPageName()).get(utilityExpensesSelectionsCoordinates.getInputName());

        boolean assetsAndIncomeBelowThreshold = assets <= ASSET_THRESHOLD && income < INCOME_THRESHOLD;
        boolean migrantWorkerAndAssetsBelowThreshold = isMigrantWorker.equals("true") && assets <= ASSET_THRESHOLD;
        int standardDeduction = utilityDeductionCalculator.calculate(utilityExpensesSelections.getValue());

        return assetsAndIncomeBelowThreshold
                || migrantWorkerAndAssetsBelowThreshold
                || (assets + income) < (housingCosts + standardDeduction);
    }

    private static double getDouble(PagesData pagesData, PageInputCoordinates pageInputCoordinates) {
        try {
            return Double.parseDouble(
                    Optional.ofNullable(pagesData.get(pageInputCoordinates.getPageName()))
                            .map(inputDataMap -> inputDataMap.get(pageInputCoordinates.getInputName()).getValue().get(0))
                            .orElse(pageInputCoordinates.getDefaultValue())
            );
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
