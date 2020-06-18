package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class TextInputsPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:test-pages-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-text-inputs")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
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
        assertThat(driver.getTitle()).isEqualTo("nextPageTitle");
        driver.findElement(By.partialLinkText("Go Back")).click();
        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");

        assertThat(driver.findElement(By.cssSelector("input")).getAttribute("value")).isEqualTo(inputText);
    }

    @Test
    void shouldNotBeAbleToChangeValueInUneditableInputs() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        WebElement uneditableInput = driver.findElement(By.cssSelector(String.format("input[name^='%s']", "uneditableInput")));

        uneditableInput.sendKeys("new value");

        assertThat(uneditableInput.getAttribute("value")).isEqualTo("default value");
    }

    @Test
    void shouldKeepUneditableInputsAfterNavigation() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo("nextPageTitle");

        driver.findElement(By.partialLinkText("Go Back")).click();

        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");
        assertThat(driver.findElement(By.cssSelector(String.format("input[name^='%s']", "uneditableInput"))).getAttribute("value")).contains("default value");
    }
}
