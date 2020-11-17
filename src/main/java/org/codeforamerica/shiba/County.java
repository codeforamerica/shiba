package org.codeforamerica.shiba;

public enum County {
    Hennepin, Olmsted, Wabasha, Other;

    public static County valueFor(String county) {
        return switch (county) {
            case "Hennepin" -> Hennepin;
            case "Olmsted" -> Olmsted;
            case "Wabasha" -> Wabasha;
            default -> Other;
        };
    }
}
