package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class PageModelPageTest extends AbstractExistingStartTimePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-page-model.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-page-model")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    String title;
    String subtleLink;
    String subtleLinkTitle;

    @Override
    @BeforeEach
    protected void setUp() throws java.io.IOException {
        super.setUp();

        title = "first page title";
        subtleLink = "subtle link text";
        subtleLinkTitle = "subtle link title";
        staticMessageSource.addMessage("first-page-title", Locale.US, title);
        staticMessageSource.addMessage("subtle-link-text", Locale.US, subtleLink);
        staticMessageSource.addMessage("subtle-link-page-title", Locale.US, subtleLinkTitle);
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
    void shouldRenderTheConfiguredSubtleLink() {
        navigateTo("subtleLinkPage");
        assertThat(testPage.driver.findElementById("subtle-link").getText()).isEqualTo(subtleLink);
    }

    @Test
    void shouldNavigateToSubtleLinkTargetPage() {
        navigateTo("subtleLinkPage");
        testPage.clickLink(subtleLink);
        assertThat(testPage.getTitle()).isEqualTo(title);
    }

    @Test
    void shouldSubmitDataForTheConfiguredPageModel() {
        navigateTo("lastPage");

        String expectedValue = "some value";
        testPage.enter("someInput", expectedValue);
        testPage.clickContinue();

        assertThat(testPage.getInputValue("someInput")).isEqualTo(expectedValue);
    }
}
