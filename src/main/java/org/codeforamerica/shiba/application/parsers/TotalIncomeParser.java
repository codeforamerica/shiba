package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.Field.INCOME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.getFirstValue;

@Component
public class TotalIncomeParser {
    private final GrossMonthlyIncomeParser grossIncomeParser;

    public TotalIncomeParser(GrossMonthlyIncomeParser grossIncomeParser) {
        this.grossIncomeParser = grossIncomeParser;
    }

    public TotalIncome parse(ApplicationData applicationData) {
        Money last30DaysIncome = Money.parse(getFirstValue(applicationData.getPagesData(), INCOME), INCOME.getDefaultValue());
        return new TotalIncome(last30DaysIncome, grossIncomeParser.parse(applicationData));
    }
}
