package org.codeforamerica.shiba.application.parsers;

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

        ParsingCoordinates laterDocsEmailCoordinates = this.parsingConfiguration.get("matchInfo");
        PageInputCoordinates matchInfoEmailCoordinates = laterDocsEmailCoordinates.getPageInputs().get("email");

        String regularFlowEmail = applicationData.getValue(emailCoordinates);
        String laterDocsEmail = applicationData.getValue(matchInfoEmailCoordinates);
        if ((null == regularFlowEmail || regularFlowEmail.isEmpty()) && (null == laterDocsEmail || laterDocsEmail.isEmpty())) {
            return Optional.empty();
        } else if (!(null == laterDocsEmail) && !laterDocsEmail.isEmpty()) {
            return Optional.of(laterDocsEmail);
        }

        return Optional.of(regularFlowEmail);
    }
}
