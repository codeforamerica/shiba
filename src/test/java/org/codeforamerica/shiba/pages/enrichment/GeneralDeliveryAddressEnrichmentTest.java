package org.codeforamerica.shiba.pages.enrichment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class GeneralDeliveryAddressEnrichmentTest {

  private final CountyMap<RoutingDestination> countyZipCodeMap = new CountyMap<>();
  private final CityInfoConfiguration cityInfoConfiguration = new CityInfoConfiguration();

  private final GeneralDeliveryAddressEnrichment generalDeliveryAddressEnrichment = new GeneralDeliveryAddressEnrichment(
      cityInfoConfiguration, countyZipCodeMap);

  @Test
  void shouldMapCityInfoForKnownCounty() {
    countyZipCodeMap.getCounties()
        .put(County.OtterTail, new RoutingDestination(null, null, null, "123-4567"));
    cityInfoConfiguration.getCityToZipAndCountyMapping().put("Battle Lake",
        Map.of("displayName", "Battle Lake", "zipcode", "56515", "county", "OtterTail"));
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", List.of("Battle Lake"))
        .build();

    EnrichmentResult enrichmentResult = generalDeliveryAddressEnrichment
        .process(applicationData.getPagesData());

    assertThat(enrichmentResult).containsEntry("enrichedCounty",
        new InputData(List.of(County.OtterTail.displayName() + " County")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedZipcode", new InputData(List.of("56515-9999")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedPhoneNumber", new InputData(List.of("123-4567")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedStreetAddress", new InputData(List.of("Battle Lake, MN")));
  }
}
