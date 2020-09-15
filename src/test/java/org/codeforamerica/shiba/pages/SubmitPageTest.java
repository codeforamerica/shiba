package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.*;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
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
    ApplicationSubmittedListener applicationSubmittedListener;

    @MockBean
    ApplicationFactory applicationFactory;

    @MockBean
    ApplicationRepository applicationRepository;

    @Test
    void shouldProvideTimestampToTerminalPageWhenApplicationIsSigned() {
        String applicationId = "someId";
        County county = County.HENNEPIN;
        when(applicationFactory.newApplication(any(), any(), any()))
                .thenReturn(Application.builder()
                        .id(applicationId)
                        .completedAt(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 11, 10), ZoneOffset.UTC))
                        .applicationData(new ApplicationData())
                        .county(county)
                        .fileName("")
                        .timeToComplete(null)
                        .build());

        navigateTo("firstPage");

        testPage.enterInput("foo", "some value");
        testPage.clickPrimaryButton();

        assertThat(driver.findElement(By.id("submission-time")).getText()).isEqualTo("2020-01-01T05:10-06:00[America/Chicago]");
        assertThat(driver.findElement(By.id("application-id")).getText()).isEqualTo(applicationId);
        assertThat(driver.findElement(By.id("county")).getText()).isEqualTo(county.name());
    }
}
