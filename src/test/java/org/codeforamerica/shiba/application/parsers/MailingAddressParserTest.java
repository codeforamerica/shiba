package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.enrichment.Address;
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
class MailingAddressParserTest {
    @Autowired
    MailingAddressParser mailingAddressParser;

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
    void shouldParseApplicationData() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData homePageData = new PageData();
        String street = "street address";
        String city = "city address";
        String state = "state address";
        String zipcode = "zipcode address";
        String apartment = "addressLine5";
        homePageData.put("addressLine1", InputData.builder().value(List.of(street)).build());
        homePageData.put("addressLine2", InputData.builder().value(List.of(city)).build());
        homePageData.put("addressLine3", InputData.builder().value(List.of(state)).build());
        homePageData.put("addressLine4", InputData.builder().value(List.of(zipcode)).build());
        homePageData.put("addressLine5", InputData.builder().value(List.of(apartment)).build());
        pagesData.put("mailingAddressPageName", homePageData);
        applicationData.setPagesData(pagesData);

        Address address = mailingAddressParser.parse(applicationData);

        assertThat(address).isEqualTo(new Address(street, city, state, zipcode, apartment, null));
    }
}