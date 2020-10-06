package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.parsers.MailingAddressParser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MailingAddressEnrichment extends AddressEnrichment {
    public MailingAddressEnrichment(
            MailingAddressParser mailingAddressParser,
            LocationClient locationClient,
            Map<String, County> countyZipCodeMap) {
        this.parser = mailingAddressParser;
        this.locationClient = locationClient;
        this.countyZipCodeMap = countyZipCodeMap;
    }
}
