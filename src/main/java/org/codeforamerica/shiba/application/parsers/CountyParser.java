package org.codeforamerica.shiba.application.parsers;

import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.*;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class CountyParser {

  private final FeatureFlagConfiguration featureFlagConfiguration;
  private final CityInfoConfiguration cityInfoConfiguration;

  public CountyParser(FeatureFlagConfiguration featureFlagConfiguration,
      CityInfoConfiguration cityInfoConfiguration) {
    this.featureFlagConfiguration = featureFlagConfiguration;
    this.cityInfoConfiguration = cityInfoConfiguration;
  }

  // Take an applicationData and figure out what county the client lives in
  public County parse(ApplicationData applicationData) {
    String countyName = parseCountyInput(applicationData);

    String countyConfigName = County.valueFor(countyName).name().toLowerCase();
    if (featureFlagConfiguration.get("county-" + countyConfigName) == FeatureFlag.OFF) {
      return County.Other;
    }
    return County.valueFor(countyName);
  }

  /**
   * Parse County input from application regardless if county is active or not.
   *
   * @param applicationData applicant data to parse
   * @return county input
   */
  public String parseCountyInput(ApplicationData applicationData) {
    String countyName;
    if (applicationData.getFlow() == FlowType.LATER_DOCS ||
        featureFlagConfiguration.get("use-county-selection") == FeatureFlag.ON) {
      countyName = getFirstValue(applicationData.getPagesData(), IDENTIFY_COUNTY);
    } else if (shouldUseGeneralDeliveryCityToCountyMap(applicationData)) {
      var cityToCountyMap = cityInfoConfiguration.getCityToZipAndCountyMapping();
      String city = applicationData.getPagesData()
          .getPageInputFirstValue("cityForGeneralDelivery", "whatIsTheCity");
      countyName = cityToCountyMap.get(city).get("county");
    } else {
      countyName = parseCountyNameFromFullApplication(applicationData);
    }
    return countyName;
  }

  private String parseCountyNameFromFullApplication(ApplicationData applicationData) {
    boolean useMailingAddress = shouldUseMailingAddress(applicationData);
    Field countySource = useMailingAddress ? MAILING_COUNTY : HOME_COUNTY;
    return getFirstValue(applicationData.getPagesData(), countySource);
  }

  private boolean shouldUseMailingAddress(ApplicationData applicationData) {
    boolean isHomeless = ofNullable(getFirstValue(applicationData.getPagesData(),
        NO_PERMANENT_ADDRESS))
        .map(Boolean::parseBoolean)
        .orElse(false);
    boolean useDifferentAddress = ofNullable(
        getFirstValue(applicationData.getPagesData(), SAME_MAILING_ADDRESS))
        .map(value -> !Boolean.parseBoolean(value))
        .orElse(true);
    return isHomeless && useDifferentAddress;
  }

  private boolean shouldUseGeneralDeliveryCityToCountyMap(ApplicationData applicationData) {
    boolean isHomeless = ofNullable(getFirstValue(applicationData.getPagesData(),
        NO_PERMANENT_ADDRESS))
        .map(Boolean::parseBoolean)
        .orElse(false);

    boolean hasSelectedGeneralDeliveryCity = ofNullable(
        getFirstValue(applicationData.getPagesData(), GENERAL_DELIVERY_CITY))
        .filter(city -> !city.isBlank())
        .isPresent();

    return isHomeless && hasSelectedGeneralDeliveryCity;
  }
}
