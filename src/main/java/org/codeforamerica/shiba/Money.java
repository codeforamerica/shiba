package org.codeforamerica.shiba;

import java.io.Serial;
import java.math.BigDecimal;

public class Money extends BigDecimal {
    @Serial
    private static final long serialVersionUID = -5167780381776339011L;
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    public static final Money ONE = new Money(BigDecimal.ONE);

    private Money(BigDecimal val) {
        super(val.unscaledValue(), 0);
    }

    public static Money parse(String s) {
        try {
            return new Money(new BigDecimal(s.replace(",","")));
        } catch (NumberFormatException exception) {
            throw new NumberFormatException("Money can't be parsed from string: " + s);
        }
    }

    @Override
    public Money add(BigDecimal bd) {
        return new Money(super.add(bd));
    }

    @Override
    public Money multiply(BigDecimal bd) {
        return new Money(super.multiply(bd));
    }

    public boolean equalTo(Money money) {
        return this.compareTo(money) == 0;
    }

    public boolean greaterOrEqualTo(Money money) {
        return this.compareTo(money) >= 0;
    }

    public boolean lessOrEqualTo(Money money) {
        return this.compareTo(money) <= 0;
    }

    public boolean greaterThan(Money money) {
        return this.compareTo(money) > 0;
    }

    public boolean lessThan(Money money) {
        return this.compareTo(money) < 0;
    }
}
