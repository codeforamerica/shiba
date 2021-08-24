package org.codeforamerica.shiba;

public enum County {
  Aitkin("Aitkin"), Anoka("Anoka"), Becker("Becker"),
  Beltrami("Beltrami"), Benton("Benton"), BigStone("Big Stone"),
  BlueEarth("Blue Earth"), Brown("Brown"), Carlton("Carlton"),
  Carver("Carver"), Cass("Cass"), Chippewa("Chippewa"),
  Chisago("Chisago"), Clay("Clay"), Clearwater("Clearwater"),
  Cook("Cook"), Cottonwood("Cottonwood"), CrowWing("Crow Wing"),
  Dakota("Dakota"), Dodge("Dodge"), Douglas("Douglas"),
  Faribault("Faribault"), Fillmore("Fillmore"), Freeborn("Freeborn"),
  Goodhue("Goodhue"), Grant("Grant"), Hennepin("Hennepin"),
  Houston("Houston"), Hubbard("Hubbard"), Isanti("Isanti"),
  Itasca("Itasca"), Jackson("Jackson"), Kanabec("Kanabec"),
  Kandiyohi("Kandiyohi"), Kittson("Kittson"), Koochiching("Koochiching"),
  LacQuiParle("Lac Qui Parle"), Lake("Lake"), LakeOfTheWoods("Lake of the Woods"),
  LeSueur("Le Sueur"), Lincoln("Lincoln"), Lyon("Lyon"),
  Mahnomen("Mahnomen"), Marshall("Marshall"), Martin("Martin"),
  McLeod("McLeod"), Meeker("Meeker"), MilleLacs("Mille Lacs"),
  Morrison("Morrison"), Mower("Mower"), Murray("Murray"),
  Nicollet("Nicollet"), Nobles("Nobles"), Norman("Norman"),
  Olmsted("Olmsted"), OtterTail("Otter Tail"), Pennington("Pennington"),
  Pine("Pine"), Pipestone("Pipestone"), Polk("Polk"),
  Pope("Pope"), Ramsey("Ramsey"), RedLake("Red Lake"),
  Redwood("Redwood"), Renville("Renville"), Rice("Rice"),
  Rock("Rock"), Roseau("Roseau"), Scott("Scott"),
  Sherburne("Sherburne"), Sibley("Sibley"), Stearns("Stearns"),
  Steele("Steele"), Stevens("Stevens"), StLouis("St. Louis"),
  Swift("Swift"), Todd("Todd"), Traverse("Traverse"),
  Wabasha("Wabasha"), Wadena("Wadena"), Waseca("Waseca"),
  Washington("Washington"), Watonwan("Watonwan"), Wilkin("Wilkin"),
  Winona("Winona"), Wright("Wright"), YellowMedicine("Yellow Medicine"),
  Other("Other");

  private final String displayName;

  County(String displayName) {
    this.displayName = displayName;
  }

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

  public String displayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
