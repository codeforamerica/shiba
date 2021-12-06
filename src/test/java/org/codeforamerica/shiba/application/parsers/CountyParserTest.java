package org.codeforamerica.shiba.application.parsers;

import static org.assertj.core.api.Assertions.assertThat;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class CountyParserTest {

  @Test
  void shouldParseCounty() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "Olmsted")
        .build();

    County county = CountyParser.parse(applicationData);

    assertThat(county).isEqualTo(County.Olmsted);
  }

  @Test
  void shouldParseCountyWithASpace() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "Otter Tail")
        .build();

    County county = CountyParser.parse(applicationData);

    assertThat(county).isEqualTo(County.OtterTail);
  }

  @Test
  void shouldUseDefaultValueWhenCountyIsNotPresent() {
    ApplicationData applicationData = new ApplicationData();
    County county = CountyParser.parse(applicationData);

    assertThat(county).isEqualTo(County.Other);
  }

  @Test
  void shouldErrorWhenCountyIsUnknown() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "not a county")
        .build();

    assertThat(CountyParser.parse(applicationData)).isEqualTo(County.Other);
  }

  @Test
  void shouldMapCountiesWithSpaces() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "Le Sueur")
        .build();

    assertThat(CountyParser.parse(applicationData)).isEqualTo(County.LeSueur);
  }

  @Test
  void shouldMapCountiesWithDots() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "St. Louis")
        .build();

    assertThat(CountyParser.parse(applicationData)).isEqualTo(County.StLouis);
  }
}
