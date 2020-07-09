package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class MidFlowRedirectPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:test-pages-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-mid-flow-redirect")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @Test
    void shouldRedirectToFirstLandingPageWhenNavigateToAMidFlowPageDirectly() {
        String expectedPageTitle = "first page title";
        staticMessageSource.addMessage("first-page-title", Locale.US, expectedPageTitle);
        staticMessageSource.addMessage("fourth-page-title", Locale.US, "fourth page title");
        driver.navigate().to(baseUrl + "/pages/fourthPage");

        assertThat(testPage.getTitle()).isEqualTo(expectedPageTitle);
    }

    @Test
    void shouldNotRedirectToFirstLandingPageWhenNavigateToAMidFlowPageAfterStartTimerPage() {
        staticMessageSource.addMessage("first-page-title", Locale.US, "first page title");
        String expectedPageTitle = "fourth page title";
        staticMessageSource.addMessage("fourth-page-title", Locale.US, expectedPageTitle);
        driver.navigate().to(baseUrl + "/pages/thirdPage");
        driver.navigate().to(baseUrl + "/pages/fourthPage");

        assertThat(testPage.getTitle()).isEqualTo(expectedPageTitle);
    }
}
