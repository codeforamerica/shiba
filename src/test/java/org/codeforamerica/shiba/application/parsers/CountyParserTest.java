package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class CountyParserTest {
    @Autowired
    CountyParser countyParser;
    private ApplicationData applicationData;

    @TestConfiguration
    @PropertySource(value = "classpath:test-parsing-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @SuppressWarnings("ConfigurationProperties")
        @Bean
        @ConfigurationProperties(prefix = "test-parsing")
        public ParsingConfiguration parsingConfiguration() {
            return new ParsingConfiguration();
        }
    }

    @BeforeEach
    void setUp() { applicationData = new ApplicationData(); }

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