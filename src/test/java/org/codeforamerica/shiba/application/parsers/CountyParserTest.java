package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class CountyParserTest {
    @Autowired
    CountyParser countyParser;

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

    @Test
    void shouldParseCounty() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData homePageData = new PageData();
        homePageData.put("addressLine6", InputData.builder().value(List.of("Olmsted")).build());
        pagesData.put("homeAddressPageName", homePageData);
        applicationData.setPagesData(pagesData);

        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.Olmsted);
    }

    @Test
    void shouldUseDefaultValueWhenCountyIsNotPresent() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData homePageData = new PageData();
        pagesData.put("homeAddressPageName", homePageData);
        applicationData.setPagesData(pagesData);

        County county = countyParser.parse(applicationData);

        assertThat(county).isEqualTo(County.Hennepin);
    }

    @Test
    void shouldUseDefaultValueWhenCountyIsNotAKnownCounty() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData homePageData = new PageData();
        pagesData.put("homeAddressPageName", homePageData);
        homePageData.put("addressLine6", InputData.builder().value(List.of("not a county")).build());
        applicationData.setPagesData(pagesData);

        assertThat(countyParser.parse(applicationData)).isEqualTo(County.Other);
    }
}