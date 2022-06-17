package org.codeforamerica.shiba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void parseConvertsUserInputDollarAmountsIntoBigDecimalsRoundDown() {
    assertThat(Money.parse("1,000")).isEqualTo(new BigDecimal("1000.00"));
    assertThat(Money.parse("10.556")).isEqualTo(new BigDecimal("10.55"));
    assertThat(Money.parse("0")).isEqualTo(new BigDecimal("0.00"));
    NumberFormatException exception = assertThrows(NumberFormatException.class,
        () -> Money.parse("asdf"));
    assertThat(exception.getMessage()).isEqualTo("Money can't be parsed from string: asdf");
  }

  @Test
  void parseWithDefaultReturnsDefaultValueIfExceptionIsThrown() {
    assertThat(Money.parse("1,000", "unusedDefault")).isEqualTo(new BigDecimal("1000.00"));
    assertThat(Money.parse("asdf", "24.56")).isEqualTo(new BigDecimal("24.56"));
  }
}