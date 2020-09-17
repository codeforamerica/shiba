package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.output.caf.AbstractApplicationDataParser;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.ParsingConfiguration;
import org.codeforamerica.shiba.output.caf.ParsingCoordinates;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TotalIncomeParser extends AbstractApplicationDataParser<TotalIncome> {
    private final ParsingCoordinates expeditedEligibilityConfiguration;
    private final AbstractApplicationDataParser<List<JobIncomeInformation>> grossIncomeParser;

    public TotalIncomeParser(ParsingConfiguration parsingConfiguration,
                             AbstractApplicationDataParser<List<JobIncomeInformation>> grossIncomeParser) {
        this.grossIncomeParser = grossIncomeParser;
        this.parsingConfiguration = parsingConfiguration;
        this.expeditedEligibilityConfiguration = parsingConfiguration.get("expeditedEligibility");
    }
    @Override
    public TotalIncome parse(ApplicationData applicationData) {
        double income = getDouble(applicationData.getPagesData(), expeditedEligibilityConfiguration.getPageInputs().get("income"));
        return new TotalIncome(income, grossIncomeParser.parse(applicationData));
    }
}
