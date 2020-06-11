package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.StaticMessageSource;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class TextInputsInteractionTest extends AbstractBasePageTest {
    @Autowired
    private MessageSource messageSource;

    @TestConfiguration
    @PropertySource(value = "classpath:test-screens-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestMessageSourceConfiguration {
        @Bean
        public MessageSource messageSource() {
            return new StaticMessageSource();
        }

        @Bean
        @ConfigurationProperties(prefix = "test-text-inputs")
        public Screens screens() {
            return new Screens();
        }
    }

    private StaticMessageSource staticMessageSource;

    private String baseUrl;

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        baseUrl = String.format("http://localhost:%s", localServerPort);
        staticMessageSource = (StaticMessageSource) messageSource;
        staticMessageSource.addMessage("general.go-back", Locale.US, "Go Back");
        staticMessageSource.addMessage("first-page-title", Locale.US, "firstPageTitle");
        staticMessageSource.addMessage("next-page-title", Locale.US, "nextPageTitle");
    }

    @Test
    void shouldShowPromptAndHelpMessagesForInput() {
        String promptMessage = "prompt message";
        staticMessageSource.addMessage("prompt-message-key", Locale.US, promptMessage);
        String helpMessage = "help message";
        staticMessageSource.addMessage("help-message-key", Locale.US, helpMessage);

        driver.navigate().to(baseUrl + "/pages/firstPage");
        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");

        assertThat(driver.findElement(By.xpath(String.format("//*[text() = '%s']", promptMessage)))).isNotNull();
        assertThat(driver.findElement(By.xpath(String.format("//*[text() = '%s']", helpMessage)))).isNotNull();
    }

    @Test
    void shouldKeepInputAfterNavigation() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        String inputText = "some input";
        driver.findElement(By.cssSelector("input")).sendKeys(inputText);

        driver.findElement(By.cssSelector("button")).click();
        takeSnapShot(driver, "screenshot");
        assertThat(driver.getTitle()).isEqualTo("nextPageTitle");
        driver.findElement(By.partialLinkText("Go Back")).click();
        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");

        assertThat(driver.findElement(By.cssSelector("input")).getAttribute("value")).isEqualTo(inputText);
    }
}
