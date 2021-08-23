package org.codeforamerica.shiba.output.caf;

import java.math.BigDecimal;
import lombok.Value;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.Iteration;

@Value
public class HourlyJobIncomeInformation implements JobIncomeInformation {

  public static final BigDecimal WEEKS_IN_MONTH = new BigDecimal("4");
  Money hourlyWage;
  BigDecimal hoursAWeek;
  int indexInJobsSubworkflow;
  Iteration iteration;

  public HourlyJobIncomeInformation(String hourlyWage, String hoursAWeek,
      int indexInJobsSubworkflow, Iteration iteration) {
    this.hourlyWage = hourlyWage.isEmpty() ? null : Money.parse(hourlyWage);
    this.hoursAWeek = hoursAWeek.isEmpty() ? null : new BigDecimal(hoursAWeek.trim());
    this.indexInJobsSubworkflow = indexInJobsSubworkflow;
    this.iteration = iteration;
  }

  @Override
  public boolean isComplete() {
    return hourlyWage != null && hoursAWeek != null;
  }

  @Override
  public Money grossMonthlyIncome() {
    return hourlyWage.multiply(hoursAWeek).multiply(WEEKS_IN_MONTH);
  }
}
