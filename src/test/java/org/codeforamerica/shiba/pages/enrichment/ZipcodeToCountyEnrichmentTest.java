package org.codeforamerica.shiba.pages.enrichment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;

class ZipcodeToCountyEnrichmentTest {

  private final HashMap<String, County> countyZipCodeMap = new HashMap<>();

  private final ZipcodeToCountyEnrichment zipcodeToCountyEnrichment = new ZipcodeToCountyEnrichment(
      countyZipCodeMap);

  @Test
  void shouldMapRecognizedZipcodeToCounty() {
    PagesData pagesData = new PagesDataBuilder()
        .withPageData("identifyZipcode", "zipCode", "12345")
        .build();
    countyZipCodeMap.put("12345", County.Olmsted);

    PageData enrichmentResult = zipcodeToCountyEnrichment.process(pagesData);

    assertThat(enrichmentResult)
        .containsEntry("mappedCounty", new InputData(List.of(County.Olmsted.name())));
  }

  @Test
  void shouldMapUnrecognizedZipcodeToOther() {
    PagesData pagesData = new PagesDataBuilder()
        .withPageData("identifyZipcode", "zipCode", "00000")
        .build();

    PageData enrichmentResult = zipcodeToCountyEnrichment.process(pagesData);

    assertThat(enrichmentResult)
        .containsEntry("mappedCounty", new InputData(List.of(County.Other.name())));
  }

}
