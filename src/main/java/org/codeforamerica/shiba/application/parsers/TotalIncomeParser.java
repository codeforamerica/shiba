package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class TotalIncomeParser extends ApplicationDataParser<TotalIncome> {
    private final GrossMonthlyIncomeParser grossIncomeParser;

    public TotalIncomeParser(ParsingConfiguration parsingConfiguration,
                             GrossMonthlyIncomeParser grossIncomeParser) {
        super(parsingConfiguration);
        this.grossIncomeParser = grossIncomeParser;
    }

    @Override
    public TotalIncome parse(ApplicationData applicationData) {
        ParsingCoordinates expeditedEligibilityConfiguration = parsingConfiguration.get("snapExpeditedEligibility");
        Money last30DaysIncome = getMoney(applicationData, expeditedEligibilityConfiguration.getPageInputs().get("income"));
        return new TotalIncome(last30DaysIncome, grossIncomeParser.parse(applicationData));
    }
}
