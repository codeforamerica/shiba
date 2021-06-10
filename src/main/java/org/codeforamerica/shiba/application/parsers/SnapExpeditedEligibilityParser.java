package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityParameters;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.Field.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.Group.HOUSEHOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.Group.JOBS;

@Component
public class SnapExpeditedEligibilityParser {
    private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;

    public SnapExpeditedEligibilityParser(GrossMonthlyIncomeParser grossMonthlyIncomeParser) {
        this.grossMonthlyIncomeParser = grossMonthlyIncomeParser;
    }

    public Optional<SnapExpeditedEligibilityParameters> parse(ApplicationData applicationData) {
        List<String> thirtyDayEstimate = parseValues(JOBS, LAST_THIRTY_DAYS_JOB_INCOME, applicationData);

        PagesData pagesData = applicationData.getPagesData();
        Money assets = parseMoney(pagesData, ASSETS);
        Money last30DaysIncome = parseMoney(pagesData, INCOME);

        Money housingCosts = parseMoney(pagesData, HOUSING_COSTS);
        String isMigrantWorker = getFirstValue(pagesData, MIGRANT_WORKER);
        boolean missingUtilities = getFirstValue(pagesData, UTILITY_EXPENSES_SELECTIONS) == null;
        List<String> utilityExpensesSelections = missingUtilities ? null : getValues(pagesData, UTILITY_EXPENSES_SELECTIONS);
        boolean applicantApplyingForSnap = getValues(pagesData, APPLICANT_PROGRAMS).contains("SNAP");
        List<String> householdPrograms = parseValues(HOUSEHOLD, HOUSEHOLD_PROGRAMS, applicationData);
        boolean householdMemberApplyingForSnap = householdPrograms != null && householdPrograms.contains("SNAP");
        boolean isPreparingMealsTogether = Boolean.parseBoolean(getFirstValue(pagesData, PREPARING_MEALS_TOGETHER));

        return Optional.of(new SnapExpeditedEligibilityParameters(
                assets, last30DaysIncome, grossMonthlyIncomeParser.parse(applicationData), isMigrantWorker,
                housingCosts, utilityExpensesSelections, thirtyDayEstimate,
                applicantApplyingForSnap, householdMemberApplyingForSnap, isPreparingMealsTogether
        ));
    }

    private Money parseMoney(PagesData pagesData, Field field) {
        return Money.parse(getFirstValue(pagesData, field), field.getDefaultValue());
    }

}
