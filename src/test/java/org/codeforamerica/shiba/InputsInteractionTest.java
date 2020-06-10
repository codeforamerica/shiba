package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(locations = "classpath:InputInteractionTestScreenConfig.properties")
public class InputsInteractionTest extends AbstractBasePageTest {
    @Autowired
    private MessageSource messageSource;

    @TestConfiguration
    static class TestMessageSourceConfiguration {
        @Bean
        public MessageSource messageSource() {
            return new StaticMessageSource();
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
    }

    @Test
    void shouldShowPromptAndHelpMessagesForInput() {
        String promptMessage = "prompt message";
        staticMessageSource.addMessage("prompt-message-key", Locale.US, promptMessage);
        String helpMessage = "help message";
        staticMessageSource.addMessage("help-message-key", Locale.US, helpMessage);
        driver.navigate().to(baseUrl + "/pages/screenName");
        assertThat(driver.findElement(By.xpath(String.format("//*[text() = '%s']", promptMessage)))).isNotNull();
        assertThat(driver.findElement(By.xpath(String.format("//*[text() = '%s']", helpMessage)))).isNotNull();
    }

    @Test
    void shouldKeepTextAfterNavigation() {
        driver.navigate().to(baseUrl + "/pages/screenName");

        String inputText = "some input";
        enterText(inputText);

        driver.findElement(By.cssSelector("button")).click();

        driver.findElement(By.partialLinkText("Go Back")).click();

        assertThat(driver.findElement(By.cssSelector("input")).getAttribute("value")).isEqualTo(inputText);
    }

    @Test
    void shouldDisplayDataEnteredFromAPreviousPage() {
        driver.navigate().to(baseUrl + "/pages/screenName");
        String inputText = "some input";
        enterText(inputText);

        driver.findElement(By.cssSelector("button")).click();
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.findElement(By.xpath(String.format("//*[text() = '%s']", inputText)))).isNotNull();
    }

    void enterText(String text) {
        driver.findElement(By.cssSelector("input")).sendKeys(text);
    }
}
