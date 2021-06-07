package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Optional.ofNullable;

@Component
public class CountyParser extends ApplicationDataParser<County> {
    private final FeatureFlagConfiguration featureFlagConfiguration;

    public CountyParser(ParsingConfiguration parsingConfiguration, FeatureFlagConfiguration featureFlagConfiguration) {
        super(parsingConfiguration);
        this.featureFlagConfiguration = featureFlagConfiguration;
    }

    @Override
    public County parse(ApplicationData applicationData) {
        String countyName = parseCountyNameFromFullApplication(applicationData);

        if (applicationData.getFlow() == FlowType.LATER_DOCS) {
            List<String> inputValue = ofNullable(applicationData.getPagesData().getPage("identifyCounty"))
                    .map(pageData -> pageData.get("county"))
                    .map(InputData::getValue)
                    .orElse(List.of());

            if (inputValue.size() > 0) {
                countyName = inputValue.get(0);
            } else {
                return County.Other;
            }
        }

        if (featureFlagConfiguration.get("county-" + County.valueFor(countyName).name().toLowerCase()) == FeatureFlag.OFF) {
            return County.Other;
        }
        return County.valueFor(countyName);
    }

    private String parseCountyNameFromFullApplication(ApplicationData applicationData) {
        boolean useMailingAddress = shouldUseMailingAddress(applicationData);
        String addressSource = useMailingAddress ? "mailingAddress" : "homeAddress";
        PageInputCoordinates coordinates = parsingConfiguration.get(addressSource).getPageInputs().get("county");
        return parseValue(coordinates, applicationData.getPagesData());
    }

    private boolean shouldUseMailingAddress(ApplicationData applicationData) {
        var homeAddressConfig = parsingConfiguration.get("homeAddress").getPageInputs();
        var isHomelessPageName = homeAddressConfig.get("isHomeless");
        var sameMailingAddressInputName = homeAddressConfig.get("sameMailingAddress");

        boolean isHomeless = ofNullable(parseValue(isHomelessPageName, applicationData.getPagesData()))
                .map(Boolean::parseBoolean)
                .orElse(false);
        boolean useDifferentAddress = ofNullable(parseValue(sameMailingAddressInputName, applicationData.getPagesData()))
                .map(value -> !Boolean.parseBoolean(value))
                .orElse(true);
        return isHomeless && useDifferentAddress;
    }
}
