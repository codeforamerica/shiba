package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.output.caf.HourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.NonHourlyJobIncomeInformation;
import org.codeforamerica.shiba.output.caf.TotalIncome;
import org.codeforamerica.shiba.output.caf.TotalIncomeCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class TotalIncomeCalculatorTest {
    TotalIncomeCalculator totalIncomeCalculator = new TotalIncomeCalculator();

    @Test
    void calculateReturnsIncomeWhenNoJobInfoProvided() {
        assertThat(totalIncomeCalculator.calculate(new TotalIncome(new Money(1), emptyList()))).isEqualTo(new Money(1));
    }

    @Test
    void calculateReturnsTheSumOfAllJobIncomeWhenProvided() {
        assertThat(totalIncomeCalculator.calculate(
                new TotalIncome(
                        new Money(9999),
                        List.of(
                                new NonHourlyJobIncomeInformation("EVERY_MONTH", "10", 0, null),
                                new HourlyJobIncomeInformation("25", "1", 0, null)
                        )
                )
        )).isEqualTo(new Money(110));
    }
}