package org.codeforamerica.shiba;

import java.util.OptionalInt;

public class Money {
    /*
     * What's the best way to represent money?
     *
     * What's the best type for this to return if the string is unparseable.
     *
     * - OptionalInt is technically good java because boxing???
     * - Optional<Integer> meets all of our requirements, but isn't "good Java"
     * - Null works but the responsibility is deferred to the caller
     */
    public static OptionalInt parse(String s) {
        try {
            return OptionalInt.of(((Double) Double.parseDouble(s.replace(",", ""))).intValue());
        } catch (Exception e) {
            return OptionalInt.empty();
        }
    }
}
