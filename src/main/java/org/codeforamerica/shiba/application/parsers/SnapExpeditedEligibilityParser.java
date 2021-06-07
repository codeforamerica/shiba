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
        List<String> thirtyDayEstimate = parseValues("lastThirtyDaysJobIncome", applicationData);

        PagesData pagesData = applicationData.getPagesData();
        Money assets = getMoney("assets", pagesData);
        Money last30DaysIncome = getMoney("income", pagesData);

        Money housingCosts = getMoney("housingCosts", pagesData);
        String isMigrantWorker = parseValue("migrantWorker", pagesData);
        List<String> utilityExpensesSelections = parseValues("utilityExpensesSelections", pagesData);
        boolean applicantApplyingForSnap = parseValues("applicantPrograms", pagesData).contains("SNAP");
        List<String> householdPrograms = parseValues("householdPrograms", applicationData);
        boolean householdMemberApplyingForSnap = householdPrograms != null && householdPrograms.contains("SNAP");
        boolean isPreparingMealsTogether = Boolean.parseBoolean(parseValue("preparingMealsTogether", pagesData));

        return Optional.of(new SnapExpeditedEligibilityParameters(
                assets, last30DaysIncome, grossMonthlyIncomeParser.parse(applicationData), isMigrantWorker,
                housingCosts, utilityExpensesSelections, thirtyDayEstimate,
                applicantApplyingForSnap, householdMemberApplyingForSnap, isPreparingMealsTogether
        ));
    }

    public List<String> parseValues(String pageInput, ApplicationData applicationData) {
        PageInputCoordinates coordinates = parsingConfiguration.get(pageInput);
        if (coordinates.getGroupName() != null) {
            Subworkflow iterations = applicationData.getSubworkflows().get(coordinates.getGroupName());
            if (iterations == null) {
                return null;
            }

            List<String> result = new ArrayList<>();
            for (Iteration iteration : iterations) {
                result.addAll(parseValues(pageInput, iteration.getPagesData()));
            }
            return result;
        } else {
            return parseValues(pageInput, applicationData.getPagesData());
        }
    }

    private List<String> parseValues(String pageInput, PagesData pagesData) {
        PageInputCoordinates coordinates = parsingConfiguration.get(pageInput);
        return pagesData.safeGetPageInputValue(coordinates.getPageName(), coordinates.getInputName());
    }
}
