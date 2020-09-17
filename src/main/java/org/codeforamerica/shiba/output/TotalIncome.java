package org.codeforamerica.shiba.output;

import lombok.Value;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;

import java.util.List;

@Value
public class TotalIncome {
    Double income;
    List<JobIncomeInformation> jobIncomeInformationList;
}
