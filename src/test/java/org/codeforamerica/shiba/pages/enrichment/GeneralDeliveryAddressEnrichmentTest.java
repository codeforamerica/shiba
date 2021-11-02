package org.codeforamerica.shiba.pages.enrichment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeneralDeliveryAddressEnrichmentTest {

  private final CountyMap<CountyRoutingDestination> countyZipCodeMap = new CountyMap<>();
  private final CityInfoConfiguration cityInfoConfiguration = new CityInfoConfiguration();
  private final FeatureFlagConfiguration featureFlagConfiguration = new FeatureFlagConfiguration(
      Map.of("use-county-selection", FeatureFlag.OFF));

  private final GeneralDeliveryAddressEnrichment generalDeliveryAddressEnrichment =
      new GeneralDeliveryAddressEnrichment(cityInfoConfiguration, countyZipCodeMap,
          featureFlagConfiguration);

  @BeforeEach
  void setup() {
    countyZipCodeMap.getCounties()
        .putAll(Map.of(
            County.Hennepin,
            new CountyRoutingDestination(County.Hennepin, null, null, null, "765-4321",
                new Address("123 hennepin st", "Minneapolis", "MN", "55555", null, "Hennepin")),
            County.OtterTail,
            new CountyRoutingDestination(County.OtterTail, null, null, null, "123-4567", null),
            County.Anoka,
            new CountyRoutingDestination(County.Anoka, null, null, null, "555-5555", null)));

    cityInfoConfiguration.getCityToZipAndCountyMapping().putAll(Map.of(
        "Battle Lake",
        Map.of("displayName", "Battle Lake", "zipcode", "56515", "county", "OtterTail"),
        "Minneapolis",
        Map.of("displayName", "Minneapolis", "zipcode", "55555", "county", "Hennepin")));
  }

  @Test
  void shouldMapCityInfoForCountyWithoutPostOfficeAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", "Battle Lake")
        .build();

    PageData enrichmentResult = generalDeliveryAddressEnrichment
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

  @Test
  void shouldMapCityInfoForCountyWithPostOfficeAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", "Minneapolis")
        .build();

    PageData enrichmentResult = generalDeliveryAddressEnrichment
        .process(applicationData.getPagesData());

    assertThat(enrichmentResult).containsEntry("enrichedCounty",
        new InputData(List.of(County.Hennepin.displayName() + " County")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedZipcode", new InputData(List.of("55555")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedPhoneNumber", new InputData(List.of("765-4321")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedStreetAddress", new InputData(List.of("123 hennepin st")));

  }

  @Test
  void shouldMapCityInfoAndCountyPhoneNumberIfUseCountySelectionIsOn() {
    featureFlagConfiguration.put("use-county-selection", FeatureFlag.ON);
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "Anoka")
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", "Battle Lake")
        .build();

    PageData enrichmentResult = generalDeliveryAddressEnrichment
        .process(applicationData.getPagesData());

    assertThat(enrichmentResult).containsEntry("enrichedCounty",
        new InputData(List.of(County.Anoka.displayName() + " County")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedZipcode", new InputData(List.of("56515-9999")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedPhoneNumber", new InputData(List.of("555-5555")));
    assertThat(enrichmentResult)
        .containsEntry("enrichedStreetAddress", new InputData(List.of("Battle Lake, MN")));
  }
}
