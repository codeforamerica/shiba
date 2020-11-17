package org.codeforamerica.shiba.output.caf;

import lombok.Value;

import java.util.List;

@Value
public class TotalIncome {
    Double last30DaysIncome;
    List<JobIncomeInformation> jobIncomeInformationList;
}
