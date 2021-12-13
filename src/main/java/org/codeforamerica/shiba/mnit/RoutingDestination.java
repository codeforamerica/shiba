package org.codeforamerica.shiba.mnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class RoutingDestination {

  private String dhsProviderId;
  private String email;
  private String phoneNumber;

  public abstract String getName();
}
