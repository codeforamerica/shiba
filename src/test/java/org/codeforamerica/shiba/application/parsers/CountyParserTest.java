package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.mockito.Mockito.mock;

class CountyParserTest {
    private final FeatureFlagConfiguration featureFlagConfiguration = mock(FeatureFlagConfiguration.class);
    private final CountyParser countyParser = new CountyParser(featureFlagConfiguration);

    @Test
    void shouldParseCounty() {
        ApplicationData applicationData = new TestApplicationDataBuilder()
                .withPageData("homeAddress", "enrichedCounty", List.of("Olmsted"))
                .build();

        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.Olmsted);
    }

    @Test
    void shouldParseCountyWithASpace() {
        ApplicationData applicationData = new TestApplicationDataBuilder()
                .withPageData("homeAddress", "enrichedCounty", List.of("Otter Tail"))
                .build();

        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.OtterTail);
    }

    @Test
    void shouldParseCountyFromMailingAddressWhenHomelessAndDifferentMailingAddress() {
        ApplicationData applicationData = new TestApplicationDataBuilder()
                .withPageData("mailingAddress", "enrichedCounty", List.of("Olmsted"))
                .withPageData("homeAddress", "isHomeless", List.of("true"))
                .withPageData("homeAddress", "sameAsMailingAddress", List.of("false"))
                .build();

        assertThat(countyParser.parse(applicationData)).isEqualTo(County.Olmsted);
    }

    @Test
    void shouldUseDefaultValueWhenCountyIsNotPresent() {
        ApplicationData applicationData = new ApplicationData();
        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.Other);
    }

    @Test
    void shouldUseDefaultValueWhenCountyIsNotAKnownCounty() {
        ApplicationData applicationData = new TestApplicationDataBuilder()
                .withPageData("homeAddress", "enrichedCounty", List.of("not a county"))
                .build();

        assertThat(countyParser.parse(applicationData)).isEqualTo(County.Other);
    }

    @Test
    void shouldParseCountyForLaterDocs() {
        ApplicationData applicationData = new TestApplicationDataBuilder()
                .withPageData("identifyCounty", "county", List.of("Olmsted"))
                .build();
        applicationData.setFlow(LATER_DOCS);

        assertThat(countyParser.parse(applicationData)).isEqualTo(County.Olmsted);
    }

    @Test
    void shouldParseCountyForLaterDocsWhenCountyIsInvalid() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setFlow(LATER_DOCS);

        assertThat(countyParser.parse(applicationData)).isEqualTo(County.Other);
    }
}