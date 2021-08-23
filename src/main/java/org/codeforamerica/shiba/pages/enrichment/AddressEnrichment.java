package org.codeforamerica.shiba.pages.enrichment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;

public abstract class AddressEnrichment implements Enrichment {

  LocationClient locationClient;
  Map<String, County> countyZipCodeMap;

  protected abstract Address parseAddress(PagesData pagesData);

  @Override
  public EnrichmentResult process(PagesData pagesData) {
    Address address = parseAddress(pagesData);
    if (address.getStreet() == null) {
      return new EnrichmentResult();
    }
    return locationClient.validateAddress(address)
        .map(validatedAddress -> Map.of(
            "enrichedStreetAddress", new InputData(List.of(validatedAddress.getStreet())),
            "enrichedCity", new InputData(List.of(validatedAddress.getCity())),
            "enrichedState", new InputData(List.of(validatedAddress.getState())),
            "enrichedZipCode", new InputData(List.of(validatedAddress.getZipcode())),
            "enrichedApartmentNumber",
            new InputData(List.of(validatedAddress.getApartmentNumber())),
            "enrichedCounty", new InputData(List.of(validatedAddress.getCounty()))
        ))
        .map(EnrichmentResult::new)
        .orElseGet(() -> Optional.ofNullable(countyZipCodeMap.get(address.getZipcode()))
            .map(county -> Map.of("enrichedCounty", new InputData(List.of(county.name()))))
            .map(EnrichmentResult::new)
            .orElse(new EnrichmentResult()));
  }
}
