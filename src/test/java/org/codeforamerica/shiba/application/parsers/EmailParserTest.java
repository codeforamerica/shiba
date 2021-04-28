package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
class EmailParserTest {
    @Autowired
    EmailParser emailParser;

    @TestConfiguration
    @PropertySource(value = "classpath:test-parsing-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @SuppressWarnings("ConfigurationProperties")
        @ConfigurationProperties(prefix = "test-parsing")
        public ParsingConfiguration parsingConfiguration() {
            return new ParsingConfiguration();
        }
    }

    ApplicationData applicationData = new ApplicationData();
    PagesData pagesData = new PagesData();
    PageData contactInfo = new PageData();

    @Test
    void shouldParseEmail() {
        String email = "email@address";
        contactInfo.put("contactEmail", InputData.builder().value(List.of(email)).build());
        pagesData.put("contactInfoPageName", contactInfo);
        applicationData.setPagesData(pagesData);

        Optional<String> parsedEmail = emailParser.parse(applicationData);

        assertThat(parsedEmail.get()).isEqualTo(email);
    }

    @Test
    void shouldParseToEmptyResult_whenEmailIsEmpty() {
        String email = "";
        contactInfo.put("contactEmail", InputData.builder().value(List.of(email)).build());
        pagesData.put("contactInfoPageName", contactInfo);
        applicationData.setPagesData(pagesData);

        Optional<String> parsedEmail = emailParser.parse(applicationData);

        assertThat(parsedEmail).isEmpty();
    }
}