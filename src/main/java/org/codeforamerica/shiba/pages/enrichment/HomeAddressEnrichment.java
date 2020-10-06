package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.parsers.HomeAddressParser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HomeAddressEnrichment extends AddressEnrichment {
    public HomeAddressEnrichment(
            HomeAddressParser homeAddressParser,
            LocationClient locationClient,
            Map<String, County> countyZipCodeMap) {
        this.parser = homeAddressParser;
        this.locationClient = locationClient;
        this.countyZipCodeMap = countyZipCodeMap;
    }
}
