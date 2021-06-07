package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

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
            PageInputCoordinates coordinates = parsingConfiguration.get("identifyCounty").getPageInputs().get("county");
            countyName = parseValue(coordinates, applicationData.getPagesData());
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
