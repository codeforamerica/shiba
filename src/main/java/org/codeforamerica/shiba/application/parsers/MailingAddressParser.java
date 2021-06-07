package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressParser extends ApplicationDataParser<Address> {
    public MailingAddressParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    @Override
    public Address parse(ApplicationData applicationData) {
        return new Address(
                parseValue("mailingStreet", applicationData.getPagesData()),
                parseValue("mailingCity", applicationData.getPagesData()),
                parseValue("mailingState", applicationData.getPagesData()),
                parseValue("mailingZipcode", applicationData.getPagesData()),
                parseValue("mailingApartmentNumber", applicationData.getPagesData()),
                null);
    }
}
