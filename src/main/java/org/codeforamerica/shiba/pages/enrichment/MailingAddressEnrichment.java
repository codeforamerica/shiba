package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.application.parsers.MailingAddressParser;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressEnrichment extends AddressEnrichment {
    public MailingAddressEnrichment(
            MailingAddressParser mailingAddressParser,
            LocationClient locationClient) {
        this.parser = mailingAddressParser;
        this.locationClient = locationClient;
    }
}
