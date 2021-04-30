package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
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
        Map<String, PageInputCoordinates> pageInputs = parsingConfiguration.get(addressSource).getPageInputs();

        return ofNullable(applicationData.getPagesData().getPage(pageInputs.get("county").getPageName()))
                .flatMap(pageData -> ofNullable(pageData.get(pageInputs.get("county").getInputName())))
                .map(inputData -> inputData.getValue().get(0))
                .orElse(pageInputs.get("county").getDefaultValue());
    }

    private boolean shouldUseMailingAddress(ApplicationData applicationData) {
        var homeAddressConfig = parsingConfiguration.get("homeAddress").getPageInputs();
        var homeAddressCoordinates = homeAddressConfig.get("county");
        var isHomelessPageName = homeAddressConfig.get("isHomeless").getInputName();
        var sameMailingAddressInputName = homeAddressConfig.get("sameMailingAddress").getInputName();

        var homeAddressCoordinatesPage = applicationData.getPagesData().getPage(homeAddressCoordinates.getPageName());
        return ofNullable(homeAddressCoordinatesPage).stream().allMatch(pageData -> {
            boolean isHomeless = ofNullable(pageData.get(isHomelessPageName))
                    .map(inputData -> inputData.getValue().equals(List.of("true")))
                    .orElse(false);
            boolean useDifferentAddress = ofNullable(pageData.get(sameMailingAddressInputName))
                    .map(inputData -> inputData.getValue().equals(List.of("false")))
                    .orElse(true);
            return isHomeless && useDifferentAddress;
        });
    }
}
