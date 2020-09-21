package org.codeforamerica.shiba;

public enum County {
    HENNEPIN, OLMSTED, OTHER;

    public static County fromString(String county) {
        return switch (county) {
            case "Hennepin" -> HENNEPIN;
            case "Olmsted" -> OLMSTED;
            default -> OTHER;
        };
    }
}
