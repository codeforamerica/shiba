package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralDeliveryAddressEnrichmentTest {
    private final CountyMap<MnitCountyInformation> countyZipCodeMap = new CountyMap<>();
    private final CityInfoConfiguration cityInfoConfiguration = new CityInfoConfiguration();

    private final GeneralDeliveryAddressEnrichment generalDeliveryAddressEnrichment = new GeneralDeliveryAddressEnrichment(cityInfoConfiguration, countyZipCodeMap);

    @Test
    void shouldMapCityInfoForKnownCountyToOther() {
        countyZipCodeMap.getCounties().put(County.Olmsted, new MnitCountyInformation(null, null, null, "123-4567"));
        cityInfoConfiguration.getCityToZipAndCountyMapping().put("Rochester", Map.of("displayName", "Rochester", "zipcode", "55901", "county", "Olmsted"));
        ApplicationData applicationData = new TestApplicationDataBuilder()
                .withPageData("cityForGeneralDelivery", "whatIsTheCity", List.of("Rochester"))
                .build();

        EnrichmentResult enrichmentResult = generalDeliveryAddressEnrichment.process(applicationData.getPagesData());

        assertThat(enrichmentResult).containsEntry("enrichedZipcode", new InputData(List.of("55901")));
        assertThat(enrichmentResult).containsEntry("enrichedPhoneNumber", new InputData(List.of("123-4567")));
    }

    @Test
    void shouldMapCityInfoForUnknownCountyToOther() {
        cityInfoConfiguration.getCityToZipAndCountyMapping().put("Ada", Map.of("displayName", "Ada", "zipcode", "56510", "county", "Norman"));
        ApplicationData applicationData = new TestApplicationDataBuilder()
                .withPageData("cityForGeneralDelivery", "whatIsTheCity", List.of("Ada"))
                .build();

        EnrichmentResult enrichmentResult = generalDeliveryAddressEnrichment.process(applicationData.getPagesData());

//        assertThat(enrichmentResult).containsEntry("enrichedZipcode", new InputData(List.of("55901")));
//        assertThat(enrichmentResult).containsEntry("enrichedPhoneNumber", new InputData(List.of("123-4567")));
    }

}