package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.Field;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.Field.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParserV2.getFirstValue;

@Component
public class CountyParser {
    private final FeatureFlagConfiguration featureFlagConfiguration;

    public CountyParser(FeatureFlagConfiguration featureFlagConfiguration) {
        this.featureFlagConfiguration = featureFlagConfiguration;
    }

    public County parse(ApplicationData applicationData) {
        String countyName;
        if (applicationData.getFlow() == FlowType.LATER_DOCS) {
            countyName = getFirstValue(applicationData.getPagesData(), IDENTIFY_COUNTY);
        } else {
            countyName = parseCountyNameFromFullApplication(applicationData);
        }

        if (featureFlagConfiguration.get("county-" + County.valueFor(countyName).name().toLowerCase()) == FeatureFlag.OFF) {
            return County.Other;
        }
        return County.valueFor(countyName);
    }

    private String parseCountyNameFromFullApplication(ApplicationData applicationData) {
        boolean useMailingAddress = shouldUseMailingAddress(applicationData);
        Field countySource = useMailingAddress ? MAILING_COUNTY : HOME_COUNTY;
        return getFirstValue(applicationData.getPagesData(), countySource);
    }

    private boolean shouldUseMailingAddress(ApplicationData applicationData) {
        boolean isHomeless = ofNullable(getFirstValue(applicationData.getPagesData(), IS_HOMELESS))
                .map(Boolean::parseBoolean)
                .orElse(false);
        boolean useDifferentAddress = ofNullable(getFirstValue(applicationData.getPagesData(), SAME_MAILING_ADDRESS))
                .map(value -> !Boolean.parseBoolean(value))
                .orElse(true);
        return isHomeless && useDifferentAddress;
    }
}
