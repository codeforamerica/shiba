package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.TotalIncome;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.ParsingConfiguration;
import org.codeforamerica.shiba.output.caf.ParsingCoordinates;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TotalIncomeParser extends ApplicationDataParser<TotalIncome> {
    private final ApplicationDataParser<List<JobIncomeInformation>> grossIncomeParser;

    public TotalIncomeParser(ParsingConfiguration parsingConfiguration,
                             ApplicationDataParser<List<JobIncomeInformation>> grossIncomeParser) {
        this.grossIncomeParser = grossIncomeParser;
        this.parsingConfiguration = parsingConfiguration;
    }

    @Override
    public TotalIncome parse(ApplicationData applicationData) {
        ParsingCoordinates expeditedEligibilityConfiguration = parsingConfiguration.get("expeditedEligibility");
        double income = getDouble(applicationData, expeditedEligibilityConfiguration.getPageInputs().get("income"));
        return new TotalIncome(income, grossIncomeParser.parse(applicationData));
    }
}
