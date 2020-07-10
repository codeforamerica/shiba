package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDecisionNavigationPageTest extends AbstractStaticMessageSourcePageTest {

    private final String optionZeroPageTitle = "option-zero-page-title";

    @TestConfiguration
    @PropertySource(value = "classpath:test-user-decision-navigation.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-user-decision-navigation")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("option-zero-page-title", Locale.US, optionZeroPageTitle);
    }

    @Test
    void shouldNavigateToOptionZeroPageWhenUserSelectOptionZero() {
        driver.navigate().to(baseUrl + "/pages/userDecisionNavigationPage");
        driver.findElement(By.partialLinkText("option 0")).click();

        assertThat(driver.getTitle()).isEqualTo(optionZeroPageTitle);
    }
}
