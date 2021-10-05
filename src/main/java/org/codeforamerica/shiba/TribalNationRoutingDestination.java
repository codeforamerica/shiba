package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.County.*;

import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.mnit.RoutingDestination;

@Data
@EqualsAndHashCode(callSuper = true)
public class TribalNationRoutingDestination extends RoutingDestination {

  // Serviced by Mille Lacs Band of Ojibwe
  public static final String BOIS_FORTE = "Bois Forte";
  public static final String FOND_DU_LAC = "Fond Du Lac";
  public static final String GRAND_PORTAGE = "Grand Portage";
  public static final String LEECH_LAKE = "Leech Lake";
  public static final String WHITE_EARTH = "White Earth";
  public static final String MILLE_LACS_BAND_OF_OJIBWE = "Mille Lacs Band of Ojibwe";

  // Not serviced by Mille Lacs Band of Ojibwe
  public static final String LOWER_SIOUX = "Lower Sioux";
  public static final String PRAIRIE_ISLAND = "Prairie Island";
  public static final String RED_LAKE = "Red Lake";
  public static final String SHAKOPEE_MDEWAKANTON = "Shakopee Mdewakanton";
  public static final String UPPER_SIOUX = "Upper Sioux";
  public static final List<County> URBAN_COUNTIES = List.of(Hennepin, Anoka, County.Ramsey);
  public static final List<County> COUNTIES_SERVICED_BY_WHITE_EARTH = List.of(Becker, Mahnomen,
      Clearwater);
  public static final List<County> MILLE_LACS_RURAL_COUNTIES = List.of(
      Aitkin, Benton, CrowWing, Morrison, MilleLacs, Pine);
  public static final Set<String> MILLE_LACS_SERVICED_TRIBES = Set.of(BOIS_FORTE, GRAND_PORTAGE,
      LEECH_LAKE, MILLE_LACS_BAND_OF_OJIBWE, WHITE_EARTH, FOND_DU_LAC);

  private String name;

  public TribalNationRoutingDestination(
      String name,
      String folderId,
      String dhsProviderId,
      String email,
      String phoneNumber) {
    super(folderId, dhsProviderId, email, phoneNumber);
    this.name = name;
  }

  public TribalNationRoutingDestination(String name) {
    super();
    this.name = name;
  }
}
