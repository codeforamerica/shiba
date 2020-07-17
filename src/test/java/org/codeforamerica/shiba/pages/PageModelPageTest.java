package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class PageModelPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-page-model.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends AbstractBasePageTest.MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-page-model")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    String title;

    @Override
    @BeforeEach
    void setUp() throws java.io.IOException {
        super.setUp();

        title = "first page title";
        staticMessageSource.addMessage("first-page-title", Locale.US, title);
    }

    @Test
    void shouldRenderThePageMatchingTheWorkflowPageName() {
        navigateTo("firstPage");

        assertThat(testPage.getTitle()).isEqualTo(title);
    }

    @Test
    void shouldRenderTheConfiguredPageModel() {
        navigateTo("lastPage");

        assertThat(testPage.getTitle()).isEqualTo(title);
    }

    @Test
    void shouldSubmitDataForTheConfiguredPageModel() {
        navigateTo("lastPage");

        String expectedValue = "some value";
        testPage.enterInput("someInput", expectedValue);
        testPage.clickPrimaryButton();

        assertThat(testPage.getInputValue("someInput")).isEqualTo(expectedValue);
    }
}
