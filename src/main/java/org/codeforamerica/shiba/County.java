package org.codeforamerica.shiba;

public enum County {
    Hennepin, Olmsted, Wabasha, Wright, Other;

    public static County valueFor(String county) {
        return switch (county) {
            case "Hennepin" -> Hennepin;
            case "Olmsted" -> Olmsted;
            case "Wabasha" -> Wabasha;
            case "Wright" -> Wright;
            default -> Other;
        };
    }
}
