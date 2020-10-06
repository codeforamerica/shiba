package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AddressEnrichment implements Enrichment {
    ApplicationDataParser<Address> parser;
    LocationClient locationClient;
    Map<String, County> countyZipCodeMap;

    @Override
    public EnrichmentResult process(ApplicationData applicationData) {
        Address address = parser.parse(applicationData);
        return locationClient.validateAddress(address)
                .map(validatedAddress -> Map.of(
                        "enrichedStreetAddress", new InputData(List.of(validatedAddress.getStreet())),
                        "enrichedCity", new InputData(List.of(validatedAddress.getCity())),
                        "enrichedState", new InputData(List.of(validatedAddress.getState())),
                        "enrichedZipCode", new InputData(List.of(validatedAddress.getZipcode())),
                        "enrichedApartmentNumber", new InputData(List.of(validatedAddress.getApartmentNumber())),
                        "enrichedCounty", new InputData(List.of(validatedAddress.getCounty()))
                ))
                .map(EnrichmentResult::new)
                .orElseGet(() -> Optional.ofNullable(countyZipCodeMap.get(address.getZipcode()))
                        .map(county -> Map.of("enrichedCounty", new InputData(List.of(county.name()))))
                        .map(EnrichmentResult::new)
                        .orElse(new EnrichmentResult()));
    }
}
