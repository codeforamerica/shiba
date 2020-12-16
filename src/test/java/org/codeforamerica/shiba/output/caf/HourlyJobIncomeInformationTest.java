package org.codeforamerica.shiba.output.caf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HourlyJobIncomeInformationTest {
    @Test
    void shouldCalculateGrossMonthlyIncome() {
        HourlyJobIncomeInformation hourlyJobIncomeInformation = new HourlyJobIncomeInformation("2", "5", 0, null);
        assertThat(hourlyJobIncomeInformation.grossMonthlyIncome()).isEqualTo(40);
    }
}