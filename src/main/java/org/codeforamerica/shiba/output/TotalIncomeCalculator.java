package org.codeforamerica.shiba.output;

import org.springframework.stereotype.Component;

@Component
public class TotalIncomeCalculator {
    public Double calculate(TotalIncome totalIncome) {
        if (totalIncome.getJobIncomeInformationList().isEmpty()) {
            return totalIncome.getIncome();
        } else {
            return totalIncome.getJobIncomeInformationList().stream().reduce(
                    0.0,
                    (total, jobIncomeInfo) -> total + jobIncomeInfo.grossMonthlyIncome(),
                    Double::sum
            );
        }
    }
}
