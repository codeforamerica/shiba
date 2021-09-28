package org.codeforamerica.shiba.mnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public abstract class RoutingDestination {

  private String folderId;
  private String dhsProviderId;
  private String email;
  private String phoneNumber;

  public abstract String getName();
}
