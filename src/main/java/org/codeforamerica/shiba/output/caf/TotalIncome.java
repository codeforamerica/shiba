package org.codeforamerica.shiba.output.caf;

import java.util.List;
import lombok.Value;
import org.codeforamerica.shiba.Money;

@Value
public class TotalIncome {

  Money last30DaysIncome;
  List<JobIncomeInformation> jobIncomeInformationList;
}
