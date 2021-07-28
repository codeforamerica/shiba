package org.codeforamerica.shiba.pages;

import java.io.Serial;

public class XfaException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 2842622389939090591L;

    public XfaException() {
        super("The PDF is Dynamic XFA.");

    }
}
