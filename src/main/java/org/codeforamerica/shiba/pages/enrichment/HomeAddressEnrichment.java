package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

@Component
public class HomeAddressEnrichment extends AddressEnrichment {
    public HomeAddressEnrichment(
            LocationClient locationClient,
            Map<String, County> countyZipCodeMap) {
        this.locationClient = locationClient;
        this.countyZipCodeMap = countyZipCodeMap;
    }

    @Override
    protected Address parseAddress(PagesData pagesData) {
        return new Address(
                getFirstValue(pagesData, HOME_STREET),
                getFirstValue(pagesData, HOME_CITY),
                getFirstValue(pagesData, HOME_STATE),
                getFirstValue(pagesData, HOME_ZIPCODE),
                getFirstValue(pagesData, HOME_APARTMENT_NUMBER),
                null);
    }
}
