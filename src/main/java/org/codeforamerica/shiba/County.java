package org.codeforamerica.shiba;

public enum County {
    Anoka("Anoka"), Carver("Carver"), Clay("Clay"), Cook("Cook"), Dodge("Dodge"),
    Hennepin("Hennepin"), Morrison("Morrisoin"), Olmsted("Olmsted"), OtterTail("Otter Tail"),
    Sherburne("Sherburne"), Steele("Steele"), StLouis("St. Louis"), Wabasha("Wabasha"),
    Wadena("Wadena"), Waseca("Waseca"), Wright("Wright"), Other("Other");

    private String displayName;

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
            case "Otter Tail", "OtterTail" -> OtterTail;
            case "Sherburne" -> Sherburne;
            case "Steele" -> Steele;
            case "St. Louis", "Saint Louis", "StLouis" -> StLouis;
            case "Wabasha" -> Wabasha;
            case "Wadena" -> Wadena;
            case "Waseca" -> Waseca;
            case "Wright" -> Wright;
            default -> Other;
        };
    }

    County(String displayName) {
        this.displayName = displayName;
    }

    public String displayName(){
        return displayName;
    }


    @Override
    public String toString() {
        return displayName;
    }
}
