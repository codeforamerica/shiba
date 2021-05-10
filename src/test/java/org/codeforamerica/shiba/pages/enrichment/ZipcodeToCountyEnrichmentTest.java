package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ZipcodeToCountyEnrichmentTest {
    private final HashMap<String, County> countyZipCodeMap = new HashMap<>();

    private final ZipcodeToCountyEnrichment zipcodeToCountyEnrichment =
            new ZipcodeToCountyEnrichment(
                    mock(FeatureFlagConfiguration.class),
                    countyZipCodeMap);

    @Test
    void shouldMapRecognizedZipcodeToCounty() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("identifyZipcode", Map.of(
                        "zipCode", List.of("12345")
                ))
        ));
        applicationData.setPagesData(pagesData);
        countyZipCodeMap.put("12345", County.Olmsted);

        EnrichmentResult enrichmentResult = zipcodeToCountyEnrichment.process(applicationData);

        assertThat(enrichmentResult).containsEntry("mappedCounty", new InputData(List.of(County.Olmsted.name())));
    }

    @Test
    void shouldMapUnrecognizedZipcodeToOther() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("identifyZipcode", Map.of(
                        "zipCode", List.of("00000")
                ))
        ));
        applicationData.setPagesData(pagesData);

        EnrichmentResult enrichmentResult = zipcodeToCountyEnrichment.process(applicationData);

        assertThat(enrichmentResult).containsEntry("mappedCounty", new InputData(List.of(County.Other.name())));
    }

}