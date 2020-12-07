package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedListener;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Sql(statements = {"TRUNCATE TABLE applications"})
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/test-submit-page.yaml"
})
public class SubmitPageTest extends AbstractBasePageTest {

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

        testPage.enter("foo", "some value");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("submission-time")).getText()).isEqualTo("2020-01-01T05:10-06:00[America/Chicago]");
        assertThat(driver.findElement(By.id("application-id")).getText()).isEqualTo(applicationId);
        assertThat(driver.findElement(By.id("county")).getText()).isEqualTo(county.name());
        assertThat(driver.findElement(By.id("sentiment")).getText()).isEqualTo(sentiment.name());
        assertThat(driver.findElement(By.id("feedback-text")).getText()).isEqualTo(feedbackText);
    }
}
