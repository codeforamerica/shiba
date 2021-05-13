package org.codeforamerica.shiba;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Money extends BigDecimal {
    @Serial
    private static final long serialVersionUID = -5167780381776339011L;
    public static final Money ZERO = new Money(0);
    public Money(double val) {
        this(new BigDecimal(Double.toString(val)).setScale(0, RoundingMode.DOWN));
    }

    public Money(BigDecimal val) {
        super(val.unscaledValue(), val.scale());
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
