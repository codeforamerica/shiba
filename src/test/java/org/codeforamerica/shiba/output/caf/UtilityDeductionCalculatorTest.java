package org.codeforamerica.shiba.output.caf;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UtilityDeductionCalculatorTest {

  private static final String heating = "HEATING";
  private static final String cooling = "COOLING";
  private static final String electricity = "ELECTRICITY";
  private static final String phone = "PHONE";
  private static final String noneOfTheAbove = "NONE_OF_THE_ABOVE";

  UtilityDeductionCalculator utilityDeductionCalculator = new UtilityDeductionCalculator();

  @SuppressWarnings("unused")
  static List<Arguments> deductionShouldBe490WhenIncludesEitherHeatingOrCooling() {
    return List.of(
        Arguments.of(List.of(heating)),
        Arguments.of(List.of(cooling)),
        Arguments.of(List.of(heating, cooling)),
        Arguments.of(List.of(heating, electricity)),
        Arguments.of(List.of(heating, phone)),
        Arguments.of(List.of(cooling, electricity)),
        Arguments.of(List.of(cooling, phone)),
        Arguments.of(List.of(heating, cooling, electricity)),
        Arguments.of(List.of(heating, cooling, phone)),
        Arguments.of(List.of(heating, phone, electricity)),
        Arguments.of(List.of(cooling, phone, electricity)),
        Arguments.of(List.of(heating, cooling, phone, electricity))
    );
  }

  @SuppressWarnings("unused")
  static List<Arguments> shouldCalculateDeductionWhenIncludesNoneOfHeatingOrCooling() {
    return List.of(
        Arguments.of(List.of(noneOfTheAbove), "0"),
        Arguments.of(List.of(electricity), "143"),
        Arguments.of(List.of(phone), "49"),
        Arguments.of(List.of(electricity, phone), "192")
    );
  }

  @ParameterizedTest
  @MethodSource
  void deductionShouldBe490WhenIncludesEitherHeatingOrCooling(List<String> utilityOptions) {
    Money deduction = utilityDeductionCalculator.calculate(utilityOptions);

    assertThat(deduction).isEqualTo(Money.parse("490"));
  }

  @ParameterizedTest
  @MethodSource
  void shouldCalculateDeductionWhenIncludesNoneOfHeatingOrCooling(
      List<String> utilityOptions,
      String expectedDeduction
  ) {
    Money deduction = utilityDeductionCalculator.calculate(utilityOptions);

    assertThat(deduction).isEqualTo(Money.parse(expectedDeduction));
  }
}
