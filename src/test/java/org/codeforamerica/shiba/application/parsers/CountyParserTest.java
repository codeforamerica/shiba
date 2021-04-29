package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CountyParserTest extends AbstractParserTest {
    CountyParser countyParser;
    private ApplicationData applicationData;

    @BeforeEach
    void setUp() {
        applicationData = new ApplicationData();
        countyParser = new CountyParser(parsingConfiguration, featureFlagConfiguration);
    }

    @Test
    void shouldParseCounty() {
        applicationData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("homeAddressPageName", Map.of(
                        "addressLine6", List.of("Olmsted"))
                )
        )));

        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.Olmsted);
    }

    @Test
    void shouldParseCountyWithASpace() {
        applicationData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("homeAddressPageName", Map.of(
                        "addressLine6", List.of("Otter Tail"))
                )
        )));

        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.OtterTail);
    }

    @Test
    void shouldParseCountyFromMailingAddressWhenHomelessAndDifferentMailingAddress() {
        applicationData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("mailingAddressPageName", Map.of(
                        "addressLine6", List.of("Olmsted"))
                ),
                new PageDataBuilder("homeAddressPageName", Map.of(
                        "addressLine7", List.of("true"),
                        "addressLine8", List.of("false"))
                )
        )));

        assertThat(countyParser.parse(applicationData)).isEqualTo(County.Olmsted);
    }

    @Test
    void shouldUseDefaultValueWhenCountyIsNotPresent() {
        applicationData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("homeAddressPageName", Map.of()
        ))));

        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.Hennepin);
    }

    @Test
    void shouldUseDefaultValueWhenCountyIsNotAKnownCounty() {
        applicationData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("homeAddressPageName", Map.of(
                        "addressLine6", List.of("not a county"))
                )
        )));

        assertThat(countyParser.parse(applicationData)).isEqualTo(County.Other);
    }
}