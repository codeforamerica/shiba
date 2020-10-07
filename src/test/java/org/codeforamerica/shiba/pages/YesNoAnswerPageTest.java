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

public class YesNoAnswerPageTest extends AbstractExistingStartTimePageTest {

    private final String answerPage = "option-zero-page-title";

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/yes-no-answer.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-yes-no-answer")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
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
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(answerPage);
        assertThat(testPage.findElementTextByName("yesOrNo")).isEqualTo("true");
    }

}
