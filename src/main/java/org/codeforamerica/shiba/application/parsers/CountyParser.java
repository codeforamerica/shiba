package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
        Map<String, PageInputCoordinates> homeAddressConfig = parsingConfiguration.get("homeAddress").getPageInputs();
        PageInputCoordinates homeAddressCoordinates = homeAddressConfig.get("county");
        boolean useMailingAddress = ofNullable(applicationData.getPagesData().getPage(homeAddressCoordinates.getPageName()))
                .stream().allMatch(pageData ->
                        ofNullable(pageData.get(homeAddressConfig.get("isHomeless").getInputName())).map(inputData -> inputData.getValue().equals(List.of("true"))).orElse(false) &&
                        ofNullable(pageData.get(homeAddressConfig.get("sameMailingAddress").getInputName())).map(inputData -> inputData.getValue().equals(List.of("false"))).orElse(true)
                );
        String addressSource = useMailingAddress ?  "mailingAddress" : "homeAddress";
        Map<String, PageInputCoordinates> pageInputs = parsingConfiguration.get(addressSource).getPageInputs();

        String countyName = ofNullable(applicationData.getPagesData().getPage(pageInputs.get("county").getPageName()))
                .flatMap(pageData -> ofNullable(pageData.get(pageInputs.get("county").getInputName())))
                .map(inputData -> inputData.getValue().get(0))
                .orElse(pageInputs.get("county").getDefaultValue());

        if (featureFlagConfiguration.get("county-" + countyName.toLowerCase()) == FeatureFlag.OFF) {
            return County.Other;
        }
        return County.valueFor(countyName);
    }
}
