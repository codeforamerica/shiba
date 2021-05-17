package org.codeforamerica.shiba.output.caf;

import lombok.Value;
import org.codeforamerica.shiba.Money;

import java.util.List;

@Value
public class TotalIncome {
    Money last30DaysIncome;
    List<JobIncomeInformation> jobIncomeInformationList;
}
