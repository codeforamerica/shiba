package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ExpeditedEligibilityParser extends AbstractApplicationDataParser<Optional<ExpeditedEligibilityParameters>> {
    private final Map<String, PageInputCoordinates> parsingConfiguration;
    private final AbstractApplicationDataParser<List<JobIncomeInformation>> jobIncomeInformationParser;

    public ExpeditedEligibilityParser(ParsingConfiguration parsingConfiguration,
                                      AbstractApplicationDataParser<List<JobIncomeInformation>> jobIncomeInformationParser) {
        this.parsingConfiguration = parsingConfiguration.get("expeditedEligibility").getPageInputs();
        this.jobIncomeInformationParser = jobIncomeInformationParser;
    }

    public Optional<ExpeditedEligibilityParameters> parse(ApplicationData applicationData) {
        PagesData pagesData = applicationData.getPagesData();

        List<String> requiredPages = parsingConfiguration.values().stream()
                .filter(PageInputCoordinates::getRequired)
                .map(PageInputCoordinates::getPageName)
                .collect(Collectors.toList());
        if (!pagesData.keySet().containsAll(requiredPages)) {
            return Optional.empty();
        }

        double assets = getDouble(pagesData, parsingConfiguration.get("assets"));
        double income = getDouble(applicationData.getPagesData(), parsingConfiguration.get("income"));

        double housingCosts = getDouble(pagesData, parsingConfiguration.get("housingCosts"));
        boolean isMigrantWorker = Boolean.parseBoolean(pagesData.getPage(parsingConfiguration.get("migrantWorker").getPageName())
                .get(parsingConfiguration.get("migrantWorker").getInputName()).getValue().get(0));
        @NotNull List<String> utilityExpensesSelections = pagesData.getPage(parsingConfiguration.get("utilityExpensesSelections").getPageName())
                .get(parsingConfiguration.get("utilityExpensesSelections").getInputName()).getValue();
        return Optional.of(new ExpeditedEligibilityParameters(assets, income, jobIncomeInformationParser.parse(applicationData), isMigrantWorker, housingCosts, utilityExpensesSelections));
    }
}
