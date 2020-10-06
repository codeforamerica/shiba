package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MailingAddressParser extends ApplicationDataParser<Address> {
    public MailingAddressParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    @Override
    public Address parse(ApplicationData applicationData) {
        Map<String, PageInputCoordinates> coordinates = parsingConfiguration.get("mailingAddress").getPageInputs();
        return new Address(
                applicationData.getValue(coordinates.get("street")),
                applicationData.getValue(coordinates.get("city")),
                applicationData.getValue(coordinates.get("state")),
                applicationData.getValue(coordinates.get("zipcode")),
                applicationData.getValue(coordinates.get("apartmentNumber")),
                null);
    }
}
