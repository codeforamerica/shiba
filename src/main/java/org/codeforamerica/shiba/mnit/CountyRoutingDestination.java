package org.codeforamerica.shiba.mnit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.enrichment.Address;

@Data
@EqualsAndHashCode(callSuper = true)
public class CountyRoutingDestination extends RoutingDestination {

  private County county;
  private Address postOfficeAddress;

  public CountyRoutingDestination(County county, String dhsProviderId,
      String email, String phoneNumber, Address postOfficeAddress) {
    super(dhsProviderId, email, phoneNumber);
    this.county = county;
    this.postOfficeAddress = postOfficeAddress;
  }

  public CountyRoutingDestination(County county, String dhsProviderId, String email,
      String phoneNumber) {
    super(dhsProviderId, email, phoneNumber);
    this.county = county;
  }

  @Override
  public String getName() {
    return county.displayName();
  }
}
