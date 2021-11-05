package org.codeforamerica.shiba.pages.enrichment;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.GENERAL_DELIVERY_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.IDENTIFY_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.documentfieldpreparers.MailingAddressStreetPreparer;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

/**
 * This will provide some default information to display to the client on the General Delivery Info
 * page as well as fill in mailing information on the pdf applications.
 * <p>
 * If the county has "postOfficeAddress" information configured, that will be used for delivery
 * mailing address.
 *
 * @see MailingAddressStreetPreparer
 * @see org.codeforamerica.shiba.mnit.CountyRoutingDestination
 */
@Component
public class GeneralDeliveryAddressEnrichment implements Enrichment {

  private final CityInfoConfiguration cityInfoConfiguration;
  private final CountyMap<CountyRoutingDestination> countyMap;
  private final FeatureFlagConfiguration featureFlagConfiguration;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public GeneralDeliveryAddressEnrichment(CityInfoConfiguration cityInfoConfiguration,
      CountyMap<CountyRoutingDestination> countyMap,
      FeatureFlagConfiguration featureFlagConfiguration) {
    this.cityInfoConfiguration = cityInfoConfiguration;
    this.countyMap = countyMap;
    this.featureFlagConfiguration = featureFlagConfiguration;
  }

  @Override
  public PageData process(PagesData pagesData) {
    String cityName = getFirstValue(pagesData, GENERAL_DELIVERY_CITY);
    Map<String, String> cityInfo = cityInfoConfiguration.getCityToZipAndCountyMapping()
        .get(cityName);
    String countyName;
    if (featureFlagConfiguration.get("use-county-selection") == FeatureFlag.ON) {
      countyName = getFirstValue(pagesData, IDENTIFY_COUNTY);
    } else {
      countyName = cityInfo.get("county").replace(" ", "");
    }
    String zipcodeFromCity = cityInfo.get("zipcode") + "-9999";
    County county = County.valueOf(countyName);
    CountyRoutingDestination countyInfo = countyMap.get(county);
    String phoneNumber = countyInfo.getPhoneNumber();
    String displayCounty = county.displayName() + " County";
    String addressFromCity = cityName + ", MN";

    List<String> enrichedAddressLines = new ArrayList<>();
    String callYourCounty = "general-delivery-address.call-your-county-to-get-the-exact-street-address";
    String tellCountyWorker = "general-delivery-address.tell-the-county-worker-you-submitted-an-application-on-MNbenefits";

    // This will show Hennepin specific post office information if the client selected Hennepin County
    // Even if the client selects a city outside of Hennepin from the city selection screen
    Address postOfficeAddress = countyInfo.getPostOfficeAddress();
    if (postOfficeAddress != null) {
      addressFromCity = postOfficeAddress.getStreet();
      zipcodeFromCity = postOfficeAddress.getZipcode();

      enrichedAddressLines.add("Main Post Office.");
      enrichedAddressLines.add(addressFromCity);
      enrichedAddressLines.add(postOfficeAddress.getCity() + ", MN " + zipcodeFromCity);
      callYourCounty += "-" + county.displayName().toLowerCase();
      tellCountyWorker += "-" + county.displayName().toLowerCase();
    } else {
      enrichedAddressLines.add(addressFromCity);
      enrichedAddressLines.add(zipcodeFromCity);
    }

    return new PageData(Map.of(
        // For filling out application
        "enrichedCounty", new InputData(List.of(displayCounty)),
        "enrichedPhoneNumber", new InputData(List.of(phoneNumber)),
        "enrichedZipcode", new InputData(List.of(zipcodeFromCity)),
        "enrichedStreetAddress", new InputData(List.of(addressFromCity)),

        // For displaying information to client
        "enrichedAddressLines", new InputData(enrichedAddressLines),
        "callYourCounty", new InputData(List.of(callYourCounty)),
        "tellCountyWorker", new InputData(List.of(tellCountyWorker))
    ));
  }
}
