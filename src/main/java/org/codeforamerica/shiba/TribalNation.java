package org.codeforamerica.shiba;

import java.util.Set;

public class TribalNation {

  public static final String BOIS_FORTE = "Bois Forte";
  public static final String FOND_DU_LAC = "Fond Du Lac";
  public static final String GRAND_PORTAGE = "Grand Portage";
  public static final String LEECH_LAKE = "Leech Lake";
  public static final String LOWER_SIOUX = "Lower Sioux";
  public static final String MILLE_LACS = "Mille Lacs";
  public static final String PRAIRIE_ISLAND = "Prairie Island";
  public static final String RED_LAKE = "Red Lake";
  public static final String SHAKOPEE_MDEWAKANTON = "Shakopee Mdewakanton";
  public static final String UPPER_SIOUX = "Upper Sioux";
  public static final String WHITE_EARTH = "White Earth";

  private static final Set<String> MILLE_LACS_SERVICED_TRIBES =
      Set.of(BOIS_FORTE, FOND_DU_LAC, GRAND_PORTAGE, LEECH_LAKE, WHITE_EARTH, MILLE_LACS);

  public static boolean isServicedByMilleLacs(String tribeName) {
    return tribeName != null && MILLE_LACS_SERVICED_TRIBES.contains(tribeName);
  }
}
