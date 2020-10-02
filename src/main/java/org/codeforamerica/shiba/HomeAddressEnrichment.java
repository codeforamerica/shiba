package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.parsers.HomeAddressParser;
import org.springframework.stereotype.Component;

@Component
public class HomeAddressEnrichment extends AddressEnrichment {
    public HomeAddressEnrichment(
            HomeAddressParser homeAddressParser,
            LocationClient locationClient) {
        this.parser = homeAddressParser;
        this.locationClient = locationClient;
    }
}
