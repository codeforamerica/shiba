package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
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

public class ConditionalRenderingPageTest extends AbstractStaticMessageSourcePageTest {

    private final String thirdPageTitle = "thirdPageTitle";
    private final String secondPageTitle = "secondPageTitle";
    private final String firstPageTitle = "firstPageTitle";

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-conditional-rendering.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-conditional-rendering")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, firstPageTitle);
        staticMessageSource.addMessage("second-page-title", Locale.US, secondPageTitle);
        staticMessageSource.addMessage("third-page-title", Locale.US, thirdPageTitle);
        staticMessageSource.addMessage("skip-message-key", Locale.US, "SKIP PAGE");
        staticMessageSource.addMessage("not-skip-message-key", Locale.US, "NOT SKIP PAGE");
    }

    @Test
    void shouldNotRenderPageAndNavigateToTheNextPageIfTheSkipConditionIsTrue() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        WebElement radioToClick = driver.findElements(By.cssSelector("span")).stream()
                .filter(webElement -> webElement.getText().equals("SKIP PAGE"))
                .findFirst()
                .orElseThrow();
        radioToClick.click();

        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);
    }

    @Test
    void shouldRenderPageIfTheSkipConditionIsFalse() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        WebElement radioToClick = driver.findElements(By.cssSelector("span")).stream()
                .filter(webElement -> webElement.getText().equals("NOT SKIP PAGE"))
                .findFirst()
                .orElseThrow();
        radioToClick.click();

        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(secondPageTitle);
    }

    @Test
    void shouldSkipGoingBackwardsAsWell() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        WebElement radioToClick = driver.findElements(By.cssSelector("span")).stream()
                .filter(webElement -> webElement.getText().equals("SKIP PAGE"))
                .findFirst()
                .orElseThrow();
        radioToClick.click();

        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);
        driver.findElement(By.partialLinkText("Back")).click();

        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldBeAbleToNavigateBackMoreThanOnePage() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("button")).click();
        assertThat(driver.getTitle()).isEqualTo(secondPageTitle);

        driver.findElement(By.cssSelector("button")).click();
        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);

        driver.findElement(By.partialLinkText("Back")).click();
        driver.findElement(By.partialLinkText("Back")).click();
        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);
    }
}
