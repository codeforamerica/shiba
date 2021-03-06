package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ZipcodeToCountyEnrichmentTest {
    private final HashMap<String, County> countyZipCodeMap = new HashMap<>();

    private final ZipcodeToCountyEnrichment zipcodeToCountyEnrichment = new ZipcodeToCountyEnrichment(countyZipCodeMap);

    @Test
    void shouldMapRecognizedZipcodeToCounty() {
        PagesData pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("identifyZipcode", Map.of(
                        "zipCode", List.of("12345")
                ))
        ));
        countyZipCodeMap.put("12345", County.Olmsted);

        EnrichmentResult enrichmentResult = zipcodeToCountyEnrichment.process(pagesData);

        assertThat(enrichmentResult).containsEntry("mappedCounty", new InputData(List.of(County.Olmsted.name())));
    }

    @Test
    void shouldMapUnrecognizedZipcodeToOther() {
        PagesData pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("identifyZipcode", Map.of(
                        "zipCode", List.of("00000")
                ))
        ));

        EnrichmentResult enrichmentResult = zipcodeToCountyEnrichment.process(pagesData);

        assertThat(enrichmentResult).containsEntry("mappedCounty", new InputData(List.of(County.Other.name())));
    }

}