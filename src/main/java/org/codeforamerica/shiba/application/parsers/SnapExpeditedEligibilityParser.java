package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityParameters;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SnapExpeditedEligibilityParser extends ApplicationDataParser<Optional<SnapExpeditedEligibilityParameters>> {
    private final ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser;

    public SnapExpeditedEligibilityParser(ParsingConfiguration parsingConfiguration,
                                          ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser) {
        super(parsingConfiguration);
        this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    }

    public Optional<SnapExpeditedEligibilityParameters> parse(ApplicationData applicationData) {
        ParsingCoordinates parsingCoordinates = parsingConfiguration.get("grossMonthlyIncome");
        List<String> thirtyDayEstimate = parseValues(parsingCoordinates, "lastThirtyDaysJobIncome", applicationData);

        Map<String, PageInputCoordinates> coordinatesMap = parsingConfiguration.get("snapExpeditedEligibility").getPageInputs();
        PagesData pagesData = applicationData.getPagesData();
        Money assets = getMoney(applicationData, coordinatesMap.get("assets"));
        Money last30DaysIncome = getMoney(applicationData, coordinatesMap.get("income"));

        Money housingCosts = getMoney(applicationData, coordinatesMap.get("housingCosts"));
        String isMigrantWorker = parseValue("migrantWorker", pagesData);
        List<String> utilityExpensesSelections = parseValues(coordinatesMap.get("utilityExpensesSelections"), pagesData);
        boolean applicantApplyingForSnap = parseValues(coordinatesMap.get("applicantPrograms"), pagesData).contains("SNAP");
        List<String> householdPrograms = parseValues(parsingConfiguration.get("snapExpeditedEligibility"), "householdPrograms", applicationData);
        boolean householdMemberApplyingForSnap = householdPrograms != null && householdPrograms.contains("SNAP");
        boolean isPreparingMealsTogether = Boolean.parseBoolean(parseValue("preparingMealsTogether", pagesData));

        return Optional.of(new SnapExpeditedEligibilityParameters(
                assets, last30DaysIncome, grossMonthlyIncomeParser.parse(applicationData), isMigrantWorker,
                housingCosts, utilityExpensesSelections, thirtyDayEstimate,
                applicantApplyingForSnap, householdMemberApplyingForSnap, isPreparingMealsTogether
        ));
    }

    public List<String> parseValues(ParsingCoordinates parsingCoordinates, String pageInput, ApplicationData applicationData) {
        PageInputCoordinates coordinates = parsingCoordinates.getPageInputs().get(pageInput);
        if (coordinates.getGroupName() != null) {
            Subworkflow iterations = applicationData.getSubworkflows().get(coordinates.getGroupName());
            if (iterations == null) {
                return null;
            }

            List<String> result = new ArrayList<>();
            for (Iteration iteration : iterations) {
                result.addAll(parseValues(coordinates, iteration.getPagesData()));
            }
            return result;
        } else {
            return parseValues(coordinates, applicationData.getPagesData());
        }
    }

    private List<String> parseValues(PageInputCoordinates coordinates, PagesData pagesData) {
        return pagesData.safeGetPageInputValue(coordinates.getPageName(), coordinates.getInputName());
    }

    private String parseValue(String pageInput, PagesData pagesData) {
        return parseValue(parsingConfiguration.get("snapExpeditedEligibility").getPageInputs().get(pageInput), pagesData);
    }
}
