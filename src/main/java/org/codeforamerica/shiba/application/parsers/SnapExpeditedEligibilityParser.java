package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityParameters;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SnapExpeditedEligibilityParser extends ApplicationDataParser<Optional<SnapExpeditedEligibilityParameters>> {
    private final ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser;

    public SnapExpeditedEligibilityParser(ParsingConfiguration parsingConfiguration,
                                          ApplicationDataParser<List<JobIncomeInformation>> grossMonthlyIncomeParser) {
        super(parsingConfiguration);
        this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    }

    public Optional<SnapExpeditedEligibilityParameters> parse(ApplicationData applicationData) {
        Map<String, PageInputCoordinates> coordinatesMap = parsingConfiguration.get("snapExpeditedEligibility").getPageInputs();
        PagesData pagesData = applicationData.getPagesData();

        List<String> requiredPages = coordinatesMap.values().stream()
                .filter(PageInputCoordinates::getRequired)
                .map(PageInputCoordinates::getPageName)
                .collect(Collectors.toList());
        if (!pagesData.keySet().containsAll(requiredPages)) {
            return Optional.empty();
        }

        boolean hasJobsSubworkflow = applicationData.getSubworkflows().get("jobs") != null;
        if (hasJobsSubworkflow) {
            Subworkflow jobsSubworkflow = applicationData.getSubworkflows().get("jobs");
            boolean allIterationsContainThirtyDayEstimate = jobsSubworkflow.stream().allMatch(iteration -> iteration.getPagesData().containsKey("lastThirtyDaysJobIncome"));
            if (allIterationsContainThirtyDayEstimate) {
                boolean shouldNotDetermineEligibilityFromAllBlankEstimates = jobsSubworkflow.stream().allMatch(iteration -> iteration.getPagesData().getPage("lastThirtyDaysJobIncome").get("lastThirtyDaysJobIncome").getValue(0).isBlank());
                if (shouldNotDetermineEligibilityFromAllBlankEstimates) {
                    return Optional.empty();
                }
            }
        }

        Money assets = getMoney(applicationData, coordinatesMap.get("assets"));
        Money last30DaysIncome = getMoney(applicationData, coordinatesMap.get("income"));

        Money housingCosts = getMoney(applicationData, coordinatesMap.get("housingCosts"));
        boolean isMigrantWorker = Boolean.parseBoolean(parseValue("migrantWorker", pagesData));
        @NotNull List<String> utilityExpensesSelections = parseValues("utilityExpensesSelections", pagesData);
        boolean applicantApplyingForSnap = pagesData.getPage("choosePrograms").get("programs").getValue(0).contains("SNAP");
        boolean householdMemberApplyingForSnap = applicationData.getApplicantAndHouseholdMemberPrograms().contains("SNAP");
        boolean isPreparingMealsTogether = false;
        PageData preparingMealsTogetherPage = pagesData.getPage("preparingMealsTogether");
        if (preparingMealsTogetherPage != null) {
        	isPreparingMealsTogether = Boolean.parseBoolean(preparingMealsTogetherPage.get("isPreparingMealsTogether").getValue(0));
        }
        boolean applyingForSnap = applicantApplyingForSnap || (householdMemberApplyingForSnap && isPreparingMealsTogether);
        return Optional.of(new SnapExpeditedEligibilityParameters(assets, last30DaysIncome, grossMonthlyIncomeParser.parse(applicationData), isMigrantWorker, housingCosts, utilityExpensesSelections, applyingForSnap));
    }

    private List<String> parseValues(String pageInput, PagesData pagesData) {
        PageInputCoordinates coordinates = parsingConfiguration.get("snapExpeditedEligibility").getPageInputs().get(pageInput);
        return pagesData.safeGetPageInputValue(coordinates.getPageName(), coordinates.getInputName());
    }

    private String parseValue(String pageInput, PagesData pagesData) {
        return super.parseValue(parsingConfiguration.get("snapExpeditedEligibility").getPageInputs().get(pageInput), pagesData);
    }
}
