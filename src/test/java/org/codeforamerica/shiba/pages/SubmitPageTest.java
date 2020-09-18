package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

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

    @SpyBean
    ApplicationFactory applicationFactory;

    @Test
    void shouldProvideApplicationDataToTerminalPageWhenApplicationIsSigned() {
        String applicationId = "someId";
        County county = County.HENNEPIN;
        ApplicationData applicationData = new ApplicationData();
        applicationData.setPagesData(new PagesData(Map.of("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(emptyList()).build())))));
        Sentiment sentiment = Sentiment.HAPPY;
        String feedbackText = "someFeedback";
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 11, 10), ZoneOffset.UTC))
                .applicationData(applicationData)
                .county(county)
                .fileName("")
                .timeToComplete(Duration.ofSeconds(124))
                .sentiment(sentiment)
                .feedback(feedbackText)
                .build();
        doReturn(application).when(applicationFactory).newApplication(any(), any(), any());

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
