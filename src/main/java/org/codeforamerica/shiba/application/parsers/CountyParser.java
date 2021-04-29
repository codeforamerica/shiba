package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            Optional<String> countyNameOptional = parseCountyNameForLaterDocs(applicationData);
            if (countyNameOptional.isPresent()) {
                countyName = countyNameOptional.get();
            } else {
                return County.Other;
            }
        }

        if (featureFlagConfiguration.get("county-" + County.valueFor(countyName).name().toLowerCase()) == FeatureFlag.OFF) {
            return County.Other;
        }
        return County.valueFor(countyName);
    }

    @NotNull
    private Optional<String> parseCountyNameForLaterDocs(ApplicationData applicationData) {
        List<String> inputValue = ofNullable(applicationData.getPagesData().getPage("identifyCounty"))
                .map(pageData -> pageData.get("county"))
                .map(InputData::getValue)
                .orElse(List.of());

        if (inputValue.size() > 0) {
            return Optional.of(inputValue.get(0));
        }
        return Optional.empty();
    }

    private String parseCountyNameFromFullApplication(ApplicationData applicationData) {
        Map<String, PageInputCoordinates> homeAddressConfig = parsingConfiguration.get("homeAddress").getPageInputs();
        PageInputCoordinates homeAddressCoordinates = homeAddressConfig.get("county");
        boolean useMailingAddress = ofNullable(applicationData.getPagesData().getPage(homeAddressCoordinates.getPageName()))
                .stream().allMatch(pageData ->
                        ofNullable(pageData.get(homeAddressConfig.get("isHomeless").getInputName())).map(inputData -> inputData.getValue().equals(List.of("true"))).orElse(false) &&
                                ofNullable(pageData.get(homeAddressConfig.get("sameMailingAddress").getInputName())).map(inputData -> inputData.getValue().equals(List.of("false"))).orElse(true)
                );
        String addressSource = useMailingAddress ? "mailingAddress" : "homeAddress";
        Map<String, PageInputCoordinates> pageInputs = parsingConfiguration.get(addressSource).getPageInputs();

        return ofNullable(applicationData.getPagesData().getPage(pageInputs.get("county").getPageName()))
                .flatMap(pageData -> ofNullable(pageData.get(pageInputs.get("county").getInputName())))
                .map(inputData -> inputData.getValue().get(0))
                .orElse(pageInputs.get("county").getDefaultValue());
    }
}
