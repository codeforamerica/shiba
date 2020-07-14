package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

public class AdditionalDataPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:test-additional-data.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "test-additional-data")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @Test
    void shouldIncludeStaticAdditionalDataAlongWithUserEnteredData() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        testPage.choose(YesNoAnswer.YES);

        assertThat(testPage.findElementTextByName("firstPage_staticAdditionalData")).isEqualTo("someValue");
    }

    @Test
    void shouldIncludeConditionalAdditionalDataAlongWithUserEnteredData() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        testPage.choose(YesNoAnswer.NO);

        assertThat(testPage.findElementTextByName("firstPage_conditionalAdditionalData")).isEqualTo("noValue");
    }
}
