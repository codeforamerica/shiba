package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HomeAddressParser extends ApplicationDataParser<Address> {
    public HomeAddressParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    @Override
    public Address parse(ApplicationData applicationData) {
        Map<String, PageInputCoordinates> coordinates = parsingConfiguration.get("homeAddress").getPageInputs();
        return new Address(
                parseValue(coordinates.get("street"), applicationData.getPagesData()),
                parseValue(coordinates.get("city"), applicationData.getPagesData()),
                parseValue(coordinates.get("state"), applicationData.getPagesData()),
                parseValue(coordinates.get("zipcode"), applicationData.getPagesData()),
                parseValue(coordinates.get("apartmentNumber"), applicationData.getPagesData()),
                null);
    }
}
