package org.codeforamerica.shiba.pages.enrichment;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.GENERAL_DELIVERY_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class GeneralDeliveryAddressEnrichment implements Enrichment {

  private final CityInfoConfiguration cityInfoConfiguration;
  private final CountyMap<MnitCountyInformation> countyMap;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public GeneralDeliveryAddressEnrichment(CityInfoConfiguration cityInfoConfiguration,
      CountyMap<MnitCountyInformation> countyMap) {
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

    return new EnrichmentResult(Map.of(
        "enrichedCounty", new InputData(List.of(displayCounty)),
        "enrichedPhoneNumber", new InputData(List.of(phoneNumber)),
        "enrichedZipcode", new InputData(List.of(zipcodeFromCity)),
        "enrichedStreetAddress", new InputData(List.of(addressFromCity))
    ));
  }
}
