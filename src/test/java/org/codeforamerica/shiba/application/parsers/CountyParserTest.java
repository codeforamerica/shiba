package org.codeforamerica.shiba.application.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.configurations.CityInfoConfigurationFactory;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = NONE, classes = {CityInfoConfigurationFactory.class,
    CountyParser.class})
@ActiveProfiles("test")
class CountyParserTest {

  @Autowired
  private CountyParser countyParser;

  @Test
  void shouldParseCounty() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "Olmsted")
        .build();

    County county = countyParser.parse(applicationData);

    assertThat(county).isEqualTo(County.Olmsted);
  }

  @Test
  void shouldParseCountyWithASpace() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "Otter Tail")
        .build();

    County county = countyParser.parse(applicationData);

    assertThat(county).isEqualTo(County.OtterTail);
  }

  @Test
  void shouldUseDefaultValueWhenCountyIsNotPresent() {
    ApplicationData applicationData = new ApplicationData();
    County county = countyParser.parse(applicationData);

    assertThat(county).isEqualTo(County.Other);
  }

  @Test
  void shouldErrorWhenCountyIsUnknown() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("identifyCounty", "county", "not a county")
        .build();

    assertThat(countyParser.parse(applicationData)).isEqualTo(County.Other);
  }
}
