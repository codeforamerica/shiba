package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class FlowRedirectPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-flow-redirect.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-flow-redirect")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    String firstPageTitle = "first page title";
    String fourthPageTitle = "fourth page title";

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, firstPageTitle);
        staticMessageSource.addMessage("fourth-page-title", Locale.US, "fourth page title");
        staticMessageSource.addMessage("first-page-title", Locale.US, "first page title");
        staticMessageSource.addMessage("fourth-page-title", Locale.US, fourthPageTitle);
    }

    @Test
    void shouldRedirectToFirstLandingPageWhenNavigateToAMidFlowPageDirectly() {
        driver.navigate().to(baseUrl + "/pages/fourthPage");

        assertThat(testPage.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldNotRedirectToFirstLandingPageWhenNavigateToAMidFlowPageAfterStartTimerPage() {
        driver.navigate().to(baseUrl + "/pages/thirdPage");
        driver.navigate().to(baseUrl + "/pages/fourthPage");

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldRedirectToTerminalPageWhenUserBacksFromTerminalPage() {
        driver.navigate().to(baseUrl + "/pages/thirdPage");
        testPage.clickPrimaryButton();

        driver.navigate().back();

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldRedirectToTerminalPageWhenUserNavigatesToANonLandingPage() {
        driver.navigate().to(baseUrl + "/pages/thirdPage");
        testPage.clickPrimaryButton();
        driver.navigate().to(baseUrl + "/pages/thirdPage");

        assertThat(testPage.getTitle()).isEqualTo(fourthPageTitle);
    }

    @Test
    void shouldNotRedirectWhenUserNavigateToALandingPage() {
        driver.navigate().to(baseUrl + "/pages/thirdPage");
        testPage.clickPrimaryButton();
        driver.navigate().to(baseUrl + "/pages/firstPage");

        assertThat(testPage.getTitle()).isEqualTo(firstPageTitle);
    }
}
