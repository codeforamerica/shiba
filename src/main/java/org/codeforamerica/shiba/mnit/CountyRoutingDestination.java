package org.codeforamerica.shiba.mnit;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.codeforamerica.shiba.County;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CountyRoutingDestination extends RoutingDestination {

  private County county;

  @Builder
  public CountyRoutingDestination(County county, String folderId, String dhsProviderId,
      String email, String phoneNumber) {
    super(folderId, dhsProviderId, email, phoneNumber);
    this.county = county;
  }

  @Override
  public String getName() {
    return county.displayName() + " County"; //TODO handle Spanish
  }
}
