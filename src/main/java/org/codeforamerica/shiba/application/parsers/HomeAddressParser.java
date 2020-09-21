package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.output.caf.PageInputCoordinates;
import org.codeforamerica.shiba.output.caf.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HomeAddressParser extends ApplicationDataParser<Address> {
    private final Map<String, PageInputCoordinates> homeAddressInputCoordinates;

    public HomeAddressParser(ParsingConfiguration parsingConfiguration) {
        homeAddressInputCoordinates = parsingConfiguration.get("homeAddress").getPageInputs();
    }

    @Override
    public Address parse(ApplicationData applicationData) {
        return new Address(
                applicationData.getValue(homeAddressInputCoordinates.get("street")),
                applicationData.getValue(homeAddressInputCoordinates.get("city")),
                applicationData.getValue(homeAddressInputCoordinates.get("state")),
                applicationData.getValue(homeAddressInputCoordinates.get("zipcode"))
        );
    }
}
