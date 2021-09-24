package org.codeforamerica.shiba;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.mnit.RoutingDestination;

@Data
@EqualsAndHashCode(callSuper = true)
public class TribalNation extends RoutingDestination {

  public static final String BOIS_FORTE = "Bois Forte";
  public static final String FOND_DU_LAC = "Fond Du Lac";
  public static final String GRAND_PORTAGE = "Grand Portage";
  public static final String LEECH_LAKE = "Leech Lake";
  public static final String LOWER_SIOUX = "Lower Sioux";
  public static final String PRAIRIE_ISLAND = "Prairie Island";
  public static final String RED_LAKE = "Red Lake";
  public static final String SHAKOPEE_MDEWAKANTON = "Shakopee Mdewakanton";
  public static final String UPPER_SIOUX = "Upper Sioux";
  public static final String WHITE_EARTH = "White Earth";
  public static final String MILLE_LACS_BAND_OF_OJIBWE = "Mille Lacs Band of Ojibwe";

  private static final Set<String> MILLE_LACS_SERVICED_TRIBES =
      Set.of(BOIS_FORTE, FOND_DU_LAC, GRAND_PORTAGE, LEECH_LAKE, WHITE_EARTH,
          MILLE_LACS_BAND_OF_OJIBWE);

  public static boolean isServicedByMilleLacs(String tribeName) {
    return tribeName != null && MILLE_LACS_SERVICED_TRIBES.contains(tribeName);
  }

  private String name;
}
