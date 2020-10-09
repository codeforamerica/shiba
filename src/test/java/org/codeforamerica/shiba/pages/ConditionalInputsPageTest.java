package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
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

public class ConditionalInputsPageTest extends AbstractExistingStartTimePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-conditional-inputs.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-conditional-inputs")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("option1", Locale.US, "option 1");
        staticMessageSource.addMessage("option2", Locale.US, "option 2");
        staticMessageSource.addMessage("option3", Locale.US, "option 3");
    }

    @Test
    void shouldOnlyRenderInputsBasedOnCondition() {
        navigateTo("firstPage");

        testPage.enter("options", "option 1");
        testPage.clickContinue();

        assertThat(driver.findElement(By.name("option1Text[]")).isDisplayed()).isTrue();
        assertThat(driver.findElements(By.name("option2Text[]"))).isEmpty();
        assertThat(driver.findElements(By.name("option3Text[]"))).isEmpty();
    }
}
