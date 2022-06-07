package org.codeforamerica.shiba;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.mnit.RoutingDestination;

@Data
@EqualsAndHashCode(callSuper = true)
public class TribalNationRoutingDestination extends RoutingDestination {

  private String name;

  public TribalNationRoutingDestination(
      TribalNation tribalNation,
      String dhsProviderId,
      String email,
      String phoneNumber) {
    super(dhsProviderId, email, phoneNumber);
    this.name = tribalNation.toString();
  }

  public TribalNationRoutingDestination(TribalNation tribalNation) {
    super();
    this.name = tribalNation.toString();
  }
}
