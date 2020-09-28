package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.caf.PageInputCoordinates;
import org.codeforamerica.shiba.output.caf.ParsingConfiguration;
import org.codeforamerica.shiba.output.caf.ParsingCoordinates;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmailParser extends ApplicationDataParser<Optional<String>> {
    public EmailParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    @Override
    public Optional<String> parse(ApplicationData applicationData) {
        ParsingCoordinates contactInfoCoordinates = this.parsingConfiguration.get("contactInfo");
        PageInputCoordinates emailCoordinates = contactInfoCoordinates.getPageInputs().get("email");
        String email = applicationData.getValue(emailCoordinates);
        if (email.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(email);
    }
}
