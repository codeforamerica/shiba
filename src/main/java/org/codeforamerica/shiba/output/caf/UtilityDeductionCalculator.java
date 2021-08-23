package org.codeforamerica.shiba.output.caf;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.springframework.stereotype.Component;

@Component
public class UtilityDeductionCalculator {

  private static final String heating = "HEATING";
  private static final String cooling = "COOLING";
  private static final String electricity = "ELECTRICITY";
  private static final String phone = "PHONE";

  public Money calculate(List<String> utilityOptions) {
    int deduction = 0;

    if (utilityOptions.contains(heating) || utilityOptions.contains(cooling)) {
      deduction += 490;
    } else {
      if (utilityOptions.contains(electricity)) {
        deduction += 143;
      }

      if (utilityOptions.contains(phone)) {
        deduction += 49;
      }
    }

    return Money.parse(String.valueOf(deduction));
  }
}
