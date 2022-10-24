package org.codeforamerica.shiba.output;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.NonHourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.output.caf.TotalIncomeCalculator;
import org.junit.jupiter.api.Test;

class TotalIncomeCalculatorTest {

  TotalIncomeCalculator totalIncomeCalculator = new TotalIncomeCalculator();

  @Test
  void calculateReturnsIncomeWhenNoJobInfoProvided() {
	assertThat(totalIncomeCalculator.calculate(new TotalIncome(Money.ONE, emptyList())))
        .isEqualTo(Money.ONE);
  }

  @Test
  void calculateReturnsTheSumOfAllJobIncomeWhenProvided() {
    assertThat(totalIncomeCalculator.calculate(
        new TotalIncome(Money.parse("9999"),
        		List.of(new NonHourlyJobIncomeInformation("EVERY_MONTH", "10", 0, null), new HourlyJobIncomeInformation("25", "1", 0, null)))))
        .isEqualTo(Money.parse("110"));
  }
}
