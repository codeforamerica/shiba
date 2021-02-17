package org.codeforamerica.shiba;

public enum County {
    Anoka, Carver, Clay, Cook, Dodge, Hennepin, Morrison, Olmsted, OtterTail, Sherburne, Steele, StLouis, Wabasha,
    Wadena, Waseca, Wright, Other;

    public static County valueFor(String county) {
        return switch (county) {
            case "Anoka" -> Anoka;
            case "Carver" -> Carver;
            case "Clay" -> Clay;
            case "Cook" -> Cook;
            case "Dodge" -> Dodge;
            case "Hennepin" -> Hennepin;
            case "Morrison" -> Morrison;
            case "Olmsted" -> Olmsted;
            case "Otter Tail" -> OtterTail;
            case "Sherburne" -> Sherburne;
            case "Steele" -> Steele;
            case "St. Louis" -> StLouis;
            case "Wabasha" -> Wabasha;
            case "Wadena" -> Wadena;
            case "Waseca" -> Waseca;
            case "Wright" -> Wright;
            default -> Other;
        };
    }
}
