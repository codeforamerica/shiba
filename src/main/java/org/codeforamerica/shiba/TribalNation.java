package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.County.Aitkin;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Becker;
import static org.codeforamerica.shiba.County.Benton;
import static org.codeforamerica.shiba.County.Chisago;
import static org.codeforamerica.shiba.County.Clearwater;
import static org.codeforamerica.shiba.County.CrowWing;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Kanabec;
import static org.codeforamerica.shiba.County.Mahnomen;
import static org.codeforamerica.shiba.County.MilleLacs;
import static org.codeforamerica.shiba.County.Morrison;
import static org.codeforamerica.shiba.County.Pine;
import static org.codeforamerica.shiba.County.Ramsey;
import static org.codeforamerica.shiba.ServicingAgency.nameFromString;

import java.util.List;
import java.util.Set;

public enum TribalNation implements ServicingAgency {
  OtherFederallyRecognizedTribe(Constants.FEDERALLY_RECOGNIZED_TRIBE_OUTSIDE_OF_MN),

  // Serviced by Mille Lacs Band of Ojibwe
  BoisForte("Bois Forte"),
  FondDuLac("Fond Du Lac"),
  GrandPortage("Grand Portage"),
  LeechLake("Leech Lake"),
  MilleLacsBandOfOjibwe(Constants.MILLE_LACS_BAND_OF_OJIBWE),

  // Not serviced by Mille Lacs Band of Ojibwe
  WhiteEarthNation("White Earth Nation"),
  LowerSioux("Lower Sioux"),
  PrairieIsland("Prairie Island"),
  RedLakeNation("Red Lake Nation"),
  ShakopeeMdewakanton("Shakopee Mdewakanton"),
  UpperSioux("Upper Sioux");

  public static final List<County> URBAN_COUNTIES = List.of(Hennepin, Anoka, Ramsey, Chisago,
      Kanabec);
  public static final List<County> COUNTIES_SERVICED_BY_WHITE_EARTH = List.of(Becker, Mahnomen,
      Clearwater);
  public static final List<County> MILLE_LACS_RURAL_COUNTIES = List.of(
      Aitkin, Benton, CrowWing, Morrison, MilleLacs, Pine);
  public static final Set<TribalNation> MILLE_LACS_SERVICED_TRIBES = Set.of(BoisForte, GrandPortage,
      LeechLake, MilleLacsBandOfOjibwe, WhiteEarthNation, FondDuLac);
  private final String name;

  TribalNation(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public static TribalNation getFromName(String name) {
    return switch (name) {
      case Constants.MILLE_LACS_BAND_OF_OJIBWE -> MilleLacsBandOfOjibwe;
      case Constants.FEDERALLY_RECOGNIZED_TRIBE_OUTSIDE_OF_MN -> OtherFederallyRecognizedTribe;
      default -> valueOf(nameFromString(name));
    };
  }

  private static class Constants {
    public static final String MILLE_LACS_BAND_OF_OJIBWE = "Mille Lacs Band of Ojibwe";
    public static final String FEDERALLY_RECOGNIZED_TRIBE_OUTSIDE_OF_MN = "Federally recognized tribe outside of MN";
  }
}
