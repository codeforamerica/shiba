package org.codeforamerica.shiba;

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

public class YesNoAnswerPageTest extends AbstractStaticMessageSourcePageTest {

    private final String answerPage = "option-zero-page-title";

    @TestConfiguration
    @PropertySource(value = "classpath:test-pages-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "yes-no-answer")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("answer-page", Locale.US, answerPage);
    }

    @Test
    void shouldDisplaySelectedAnswer() {
        driver.navigate().to(baseUrl + "/pages/yesNoQuestionPage");
        driver.findElement(By.cssSelector("label:first-of-type")).click();

        assertThat(driver.getTitle()).isEqualTo(answerPage);
        assertThat(driver.findElement(By.cssSelector("input")).getAttribute("value")).isEqualTo("true");
    }

    @Test
    void shouldNotDisplayContinueButton() {
        driver.navigate().to(baseUrl + "/pages/yesNoQuestionPage");

        assertThat(driver.findElements(By.className("button--primary"))).isEmpty();
    }
}
