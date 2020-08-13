package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.output.ApplicationDataConsumer;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SubmitPageTest extends AbstractStaticMessageSourcePageTest {

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-submit-page.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-submit-page")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    @MockBean
    ApplicationDataConsumer applicationDataConsumer;

    @Override
    @BeforeEach
    void setUp() throws java.io.IOException {
        super.setUp();
        when(applicationDataConsumer.process(any())).thenReturn(ZonedDateTime.now());
    }

    @Test
    void shouldProvideTimestampToTerminalPageWhenApplicationDataIsConsumed() {
        when(applicationDataConsumer.process(any()))
                .thenReturn(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 11, 10), ZoneOffset.UTC));
        ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 10, 10), ZoneOffset.UTC);

        navigateTo("firstPage");

        testPage.enterInput("foo", "some value");
        testPage.clickPrimaryButton();

        assertThat(driver.findElement(By.id("submission-time")).getText()).isEqualTo("2020-01-01T11:10Z");
    }
}
