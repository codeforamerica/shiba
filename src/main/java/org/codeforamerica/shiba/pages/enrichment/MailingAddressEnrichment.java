package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

@Component
public class MailingAddressEnrichment extends AddressEnrichment {
    public MailingAddressEnrichment(
            LocationClient locationClient,
            Map<String, County> countyZipCodeMap) {
        this.locationClient = locationClient;
        this.countyZipCodeMap = countyZipCodeMap;
    }

    @Override
    protected Address parseAddress(ApplicationData applicationData) {
        return new Address(
                getFirstValue(applicationData.getPagesData(), MAILING_STREET),
                getFirstValue(applicationData.getPagesData(), MAILING_CITY),
                getFirstValue(applicationData.getPagesData(), MAILING_STATE),
                getFirstValue(applicationData.getPagesData(), MAILING_ZIPCODE),
                getFirstValue(applicationData.getPagesData(), MAILING_APARTMENT_NUMBER),
                null);
    }
}
