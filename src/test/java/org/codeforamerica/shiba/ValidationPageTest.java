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

public class ValidationPageTest extends AbstractStaticMessageSourcePageTest{

    private final String errorMessage = "error message";
    private final String nextPageTitle = "next Page Title";
    private final String lastPageTitle = "last page title";

    @TestConfiguration
    @PropertySource(value = "classpath:test-pages-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-validation")
        public PageConfiguration pageConfiguration() {
            return new PageConfiguration();
        }
    }

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, "firstPageTitle");
        staticMessageSource.addMessage("next-page-title", Locale.US, nextPageTitle);
        staticMessageSource.addMessage("last-page-title", Locale.US, lastPageTitle);
        staticMessageSource.addMessage("error-message-key", Locale.US, errorMessage);
    }

    @Test
    void shouldStayOnThePage_whenValidationFails() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");
    }

    @Test
    void shouldGoOnToNextPage_whenValidationPasses() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("something");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(nextPageTitle);
    }

    @Test
    void shouldDisplayErrorMessageWhenValidationFailed() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(getInputError("someInputName").getText()).isEqualTo(errorMessage);
    }

    @Test
    void shouldNotTriggerValidation_whenConditionIsFalse() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("do not trigger validation");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(nextPageTitle);
    }

    @Test
    void shouldTriggerValidation_whenConditionIsTrue() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("valueToTriggerCondition");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");
        assertThat(getInputError("conditionalValidationWhenValueEquals")).isNotNull();
    }

    @Test
    void shouldTriggerValidation_whenConditionInputValueIsNotPresent() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("do not trigger validation");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(nextPageTitle);

        driver.findElement(By.cssSelector("button")).click();

        assertThat(getInputError("conditionalValidationWhenValueIsNotPresent")).isNotNull();
    }

    @Test
    void shouldNotTriggerValidation_whenConditionInputValueIsPresent() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("do not trigger validation");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(nextPageTitle);
        driver.findElement(By.cssSelector("input[name^='someCheckbox']")).click();
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
    }

    WebElement getInputError(String inputName) {
        return driver.findElement(By.cssSelector(String.format("input[name^='%s'] ~ p.text--error", inputName)));
    }
}
