package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.Money;
import org.springframework.stereotype.Component;

@Component
public class TotalIncomeCalculator {
    public Money calculate(TotalIncome totalIncome) {
        if (totalIncome.getJobIncomeInformationList().isEmpty()) {
            return totalIncome.getLast30DaysIncome();
        } else {
            return totalIncome.getJobIncomeInformationList().stream().reduce(
                    Money.ZERO,
                    (total, jobIncomeInfo) -> total.add(jobIncomeInfo.grossMonthlyIncome()),
                    Money::add
            );
        }
    }
}
