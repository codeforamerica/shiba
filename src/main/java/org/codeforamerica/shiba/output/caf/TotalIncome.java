package org.codeforamerica.shiba.output.caf;

import lombok.Value;

import java.util.List;

@Value
public class TotalIncome {
    int last30DaysIncome;
    List<JobIncomeInformation> jobIncomeInformationList;
}
