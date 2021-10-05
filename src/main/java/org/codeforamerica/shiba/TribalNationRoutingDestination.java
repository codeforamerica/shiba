package org.codeforamerica.shiba;

import java.util.Set;
import lombok.Builder;
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

  private static final Set<String> MILLE_LACS_SERVICED_TRIBES =
      Set.of(BOIS_FORTE, FOND_DU_LAC, GRAND_PORTAGE, LEECH_LAKE, WHITE_EARTH,
          MILLE_LACS_BAND_OF_OJIBWE);

  private String name;
  private boolean isServicedByMilleLacs;

  public TribalNationRoutingDestination(
      String name,
      String folderId,
      String dhsProviderId,
      String email,
      String phoneNumber,
      boolean isServicedByMilleLacs) {
    super(folderId, dhsProviderId, email, phoneNumber);
    this.isServicedByMilleLacs = isServicedByMilleLacs;
    this.name = name;
  }

  public TribalNationRoutingDestination(
      String name,
      boolean isServicedByMilleLacs
  ) {
    super();
    this.isServicedByMilleLacs = isServicedByMilleLacs;
    this.name = name;
  }

  @Builder
  public TribalNationRoutingDestination(String name, String folderId, String dhsProviderId,
      String email, String phoneNumber) {
    super(folderId, dhsProviderId, email, phoneNumber);
    this.name = name;
  }
}
