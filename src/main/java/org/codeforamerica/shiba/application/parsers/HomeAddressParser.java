package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.springframework.stereotype.Component;

@Component
public class HomeAddressParser extends ApplicationDataParser<Address> {
    public HomeAddressParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    @Override
    public Address parse(ApplicationData applicationData) {
        return new Address(
                parseValue("homeStreet", applicationData.getPagesData()),
                parseValue("homeCity", applicationData.getPagesData()),
                parseValue("homeState", applicationData.getPagesData()),
                parseValue("homeZipcode", applicationData.getPagesData()),
                parseValue("homeApartmentNumber", applicationData.getPagesData()),
                null);
    }
}
