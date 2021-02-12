package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.application.parsers.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
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

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class CcapExpeditedEligibilityParserTest {
    @Autowired
    CcapExpeditedEligibilityParser ccapExpeditedEligibilityParser;

    private final ApplicationData applicationData = new ApplicationData();
    private final PagesData pagesData = new PagesData();

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
    void setUp() {
        applicationData.setPagesData(pagesData);
    }

    @Test
    void shouldParseConfiguredExpeditedEligibilityInputs() {
        pagesData.putPage("livingSituation", new PageData(Map.of("livingSituation", InputData.builder().value(List.of("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING")).build())));

        CcapExpeditedEligibilityParameters parameters = ccapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new CcapExpeditedEligibilityParameters("LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING"));
    }

    @Test
    void shouldReturnNullWhenEligibilityInputsNotAvailable() {
        CcapExpeditedEligibilityParameters parameters = ccapExpeditedEligibilityParser.parse(applicationData).get();

        assertThat(parameters).isEqualTo(new CcapExpeditedEligibilityParameters(null));
    }
}