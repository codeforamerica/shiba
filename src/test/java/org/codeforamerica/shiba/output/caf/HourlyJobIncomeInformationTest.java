package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HourlyJobIncomeInformationTest {
    @Test
    void shouldCalculateGrossMonthlyIncome() {
        HourlyJobIncomeInformation hourlyJobIncomeInformation = new HourlyJobIncomeInformation("2", "5", 0, null);
        assertThat(hourlyJobIncomeInformation.grossMonthlyIncome()).isEqualTo(Money.parse("40"));
    }

    @Test
    void shouldCalculateGrossMonthlyIncomeWithNonWholeNumberWages() {
        HourlyJobIncomeInformation hourlyJobIncomeInformation = new HourlyJobIncomeInformation("2.5", "5", 0, null);
        assertThat(hourlyJobIncomeInformation.grossMonthlyIncome()).isEqualTo(Money.parse("50"));
    }
}