package org.codeforamerica.shiba.pages.enrichment;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.GENERAL_DELIVERY_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class GeneralDeliveryAddressEnrichment implements Enrichment {

  private final CityInfoConfiguration cityInfoConfiguration;
  private final CountyMap<CountyRoutingDestination> countyMap;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public GeneralDeliveryAddressEnrichment(CityInfoConfiguration cityInfoConfiguration,
      CountyMap<CountyRoutingDestination> countyMap) {
    this.cityInfoConfiguration = cityInfoConfiguration;
    this.countyMap = countyMap;
  }

  @Override
  public EnrichmentResult process(PagesData pagesData) {
    String cityName = getFirstValue(pagesData, GENERAL_DELIVERY_CITY);
    Map<String, String> cityInfo = cityInfoConfiguration.getCityToZipAndCountyMapping()
        .get(cityName);

    String countyFromCity = cityInfo.get("county").replace(" ", "");
    String zipcodeFromCity = cityInfo.get("zipcode") + "-9999";
    County county = County.valueOf(countyFromCity);
    String phoneNumber = countyMap.get(county).getPhoneNumber();
    String displayCounty = county.displayName() + " County";
    String addressFromCity = cityName + ", MN";

    List<String> enrichedAddressLines = new ArrayList<>();
    String callYourCounty = "general-delivery-address.call-your-county-to-get-the-exact-street-address";
    String tellCountyWorker = "general-delivery-address.tell-the-county-worker-you-submitted-an-application-on-MNbenefits";

    if (county == County.Hennepin) {
      enrichedAddressLines.add("Main Post Office.");
      enrichedAddressLines.add("100 S 1st St");
      enrichedAddressLines.add(addressFromCity + " " + cityInfo.get("zipcode"));
      callYourCounty += "-hennepin";
      tellCountyWorker += "-hennepin";
    } else {
      enrichedAddressLines.add(addressFromCity);
      enrichedAddressLines.add(zipcodeFromCity);
    }

    return new EnrichmentResult(Map.of(
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
