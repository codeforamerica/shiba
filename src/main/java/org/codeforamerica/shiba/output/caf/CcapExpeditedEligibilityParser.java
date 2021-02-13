package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.PageInputCoordinates;
import org.codeforamerica.shiba.application.parsers.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CcapExpeditedEligibilityParser extends ApplicationDataParser<Optional<CcapExpeditedEligibilityParameters>> {
    public CcapExpeditedEligibilityParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    public Optional<CcapExpeditedEligibilityParameters> parse(ApplicationData applicationData) {
        Map<String, PageInputCoordinates> coordinatesMap = parsingConfiguration.get("ccapExpeditedEligibility").getPageInputs();
        PagesData pagesData = applicationData.getPagesData();

        List<String> requiredPages = coordinatesMap.values().stream()
                .filter(PageInputCoordinates::getRequired)
                .map(PageInputCoordinates::getPageName)
                .collect(Collectors.toList());
        if (!pagesData.keySet().containsAll(requiredPages)) {
            return Optional.empty();
        }

        String livingSituation = applicationData.getValue(coordinatesMap.get("livingSituation"));
        return Optional.of(new CcapExpeditedEligibilityParameters(livingSituation));
    }

}
