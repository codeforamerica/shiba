package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedListener;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Sql(statements = {"TRUNCATE TABLE applications"})
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

    @Test
    void shouldProvideApplicationDataToTerminalPageWhenApplicationIsSigned() {
        String applicationId = "someId";
        County county = County.Hennepin;
        ApplicationData applicationData = new ApplicationData();
        Sentiment sentiment = Sentiment.HAPPY;
        String feedbackText = "someFeedback";
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 11, 10), ZoneOffset.UTC))
                .applicationData(applicationData)
                .county(county)
                .timeToComplete(Duration.ofSeconds(124))
                .sentiment(sentiment)
                .feedback(feedbackText)
                .build();
        when(applicationFactory.newApplication(any(), any())).thenReturn(application);

        navigateTo("firstPage");

        testPage.enterInput("foo", "some value");
        testPage.clickPrimaryButton();

        assertThat(driver.findElement(By.id("submission-time")).getText()).isEqualTo("2020-01-01T05:10-06:00[America/Chicago]");
        assertThat(driver.findElement(By.id("application-id")).getText()).isEqualTo(applicationId);
        assertThat(driver.findElement(By.id("county")).getText()).isEqualTo(county.name());
        assertThat(driver.findElement(By.id("sentiment")).getText()).isEqualTo(sentiment.name());
        assertThat(driver.findElement(By.id("feedback-text")).getText()).isEqualTo(feedbackText);
    }
}
