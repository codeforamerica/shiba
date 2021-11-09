package org.codeforamerica.shiba.pages.enrichment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FullNameEnrichmentTest {

  FullNameEnrichment enrichment;

  @BeforeEach
  void setup() {
    enrichment = new FullNameEnrichment();
  }

  @Test
  void shouldAddFormattedFullNameEntry() {
    PagesData pagesData = new PagesDataBuilder()
        .withPageData("householdSelectionForIncome", "whoseJobIsIt", "Judy Garland applicant")
        .build();

    PageData enrichmentResult = enrichment.process(pagesData);

    assertThat(enrichmentResult)
        .containsEntry("whoseJobIsItFormatted", new InputData(List.of("Judy Garland")));
  }

  @Test
  void shouldReturnEmptyForMissingData() {
    PageData enrichmentResult = enrichment.process(new PagesData());
    assertThat(enrichmentResult).isEmpty();
  }

}
