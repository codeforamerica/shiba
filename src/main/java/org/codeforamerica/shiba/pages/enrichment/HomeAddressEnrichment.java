package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.ApplicationData;
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
    protected Address parseAddress(ApplicationData applicationData) {
        return new Address(
                getFirstValue(applicationData.getPagesData(), HOME_STREET),
                getFirstValue(applicationData.getPagesData(), HOME_CITY),
                getFirstValue(applicationData.getPagesData(), HOME_STATE),
                getFirstValue(applicationData.getPagesData(), HOME_ZIPCODE),
                getFirstValue(applicationData.getPagesData(), HOME_APARTMENT_NUMBER),
                null);
    }
}
