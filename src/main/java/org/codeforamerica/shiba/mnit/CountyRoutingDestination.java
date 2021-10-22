package org.codeforamerica.shiba.mnit;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.enrichment.Address;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CountyRoutingDestination extends RoutingDestination {

  private County county;
  private Address postOfficeAddress;

  @Builder
  public CountyRoutingDestination(County county, String folderId, String dhsProviderId,
      String email, String phoneNumber, Address postOfficeAddress) {
    super(folderId, dhsProviderId, email, phoneNumber);
    this.county = county;
    this.postOfficeAddress = postOfficeAddress;
  }

  @Override
  public String getName() {
    return county.displayName();
  }
}
