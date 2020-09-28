package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Address;
import org.codeforamerica.shiba.output.caf.PageInputCoordinates;
import org.codeforamerica.shiba.output.caf.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
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
                applicationData.getValue(coordinates.get("street")),
                applicationData.getValue(coordinates.get("city")),
                applicationData.getValue(coordinates.get("state")),
                applicationData.getValue(coordinates.get("zipcode"))
        );
    }
}
