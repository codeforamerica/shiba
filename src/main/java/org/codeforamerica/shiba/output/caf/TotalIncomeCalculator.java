package org.codeforamerica.shiba.output.caf;

import org.springframework.stereotype.Component;

@Component
public class TotalIncomeCalculator {
    public int calculate(TotalIncome totalIncome) {
        if (totalIncome.getJobIncomeInformationList().isEmpty()) {
            return totalIncome.getLast30DaysIncome();
        } else {
            return totalIncome.getJobIncomeInformationList().stream().reduce(
                    0,
                    (total, jobIncomeInfo) -> total + jobIncomeInfo.grossMonthlyIncome(),
                    Integer::sum
            );
        }
    }
}
