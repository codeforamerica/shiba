package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class EmailParser extends ApplicationDataParser<Optional<String>> {
    public EmailParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    @Override
    public Optional<String> parse(ApplicationData applicationData) {
        String laterDocsEmail = getEmail(applicationData, "matchInfo");
        if (isNotBlank(laterDocsEmail)) {
            return Optional.of(laterDocsEmail);
        }

        String regularFlowEmail = getEmail(applicationData, "contactInfo");
        if (isNotBlank(regularFlowEmail)) {
            return Optional.of(regularFlowEmail);
        }

        return Optional.empty();
    }

    private String getEmail(ApplicationData applicationData, String configName) {
        ParsingCoordinates contactInfoCoordinates = this.parsingConfiguration.get(configName);
        PageInputCoordinates emailCoordinates = contactInfoCoordinates.getPageInputs().get("email");
        return applicationData.getValue(emailCoordinates);
    }
}
