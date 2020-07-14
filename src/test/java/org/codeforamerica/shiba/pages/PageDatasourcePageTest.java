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
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class PageDatasourcePageTest extends AbstractStaticMessageSourcePageTest {
    private final String staticPageWithDatasourceTitle = "staticPageWithDatasourceTitle";
    private final String staticPageWithDatasourceInputsTitle = "staticPageWithDatasourceInputsTitle";
    private final String yesHeaderText = "yes header text";

    @TestConfiguration
    @PropertySource(value = "classpath:test-page-datasources.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-page-datasource")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, "firstPageTitle");
        staticMessageSource.addMessage("static-page-with-datasource-title", Locale.US, staticPageWithDatasourceTitle);
        staticMessageSource.addMessage("static-page-with-datasource-inputs-title", Locale.US, staticPageWithDatasourceInputsTitle);
        staticMessageSource.addMessage("yes-header-text", Locale.US, yesHeaderText);
        staticMessageSource.addMessage("general.inputs.yes", Locale.US, YesNoAnswer.YES.getDisplayValue());
        staticMessageSource.addMessage("general.inputs.no", Locale.US, YesNoAnswer.NO.getDisplayValue());
        staticMessageSource.addMessage("some-other-header", Locale.US, "some other header");
        staticMessageSource.addMessage("some-header", Locale.US, "some other header");
    }

    @Test
    void shouldDisplayDataEnteredFromAPreviousPage() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        String inputText = "some input";
        testPage.enterInput("someInputName", inputText);
        testPage.clickPrimaryButton();

        assertThat(testPage.findElementTextByName("firstPage_someInputName")).isEqualTo(inputText);
    }

    @Test
    void shouldDisplayDataEnteredFromAPreviousPage_usingMessageKeysWhenAvailable() {
        staticMessageSource.addMessage("radio-value-key-1", Locale.US, "RADIO 1");
        staticMessageSource.addMessage("radio-value-key-2", Locale.US, "RADIO 2");
        String radioValueDisplayMessage = "RADIO 2 MESSAGE";
        staticMessageSource.addMessage("display-value-key-2", Locale.US, radioValueDisplayMessage);

        driver.navigate().to(baseUrl + "/pages/firstPage");

        WebElement radioToClick = driver.findElements(By.cssSelector("span")).stream()
                .filter(webElement -> webElement.getText().equals("RADIO 2"))
                .findFirst()
                .orElseThrow();
        radioToClick.click();
        driver.findElement(By.cssSelector("button")).click();

        driver.navigate().to(baseUrl + "/pages/staticPageWithDatasourceInputs");
        assertThat(driver.getTitle()).isEqualTo(staticPageWithDatasourceInputsTitle);

        assertThat(driver.findElement(By.xpath(String.format("//*[text() = '%s']", radioValueDisplayMessage)))).isNotNull();
    }

    @Test
    void shouldDisplayPageTitleBasedOnCondition() {
        String noAnswerTitle = "no answer title";
        staticMessageSource.addMessage("foo", Locale.US, "wrong title");
        staticMessageSource.addMessage("no-answer-title", Locale.US, noAnswerTitle);

        driver.navigate().to(baseUrl + "/pages/yesNoQuestionPage");

        List<WebElement> yesNoRadios = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect = yesNoRadios.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.NO.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();

        assertThat(driver.getTitle()).isEqualTo(noAnswerTitle);
    }

    @Test
    void shouldDisplayPageHeaderBasedOnCondition() {
        driver.navigate().to(baseUrl + "/pages/yesNoQuestionPage");

        List<WebElement> yesNoRadios = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect = yesNoRadios.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.YES.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();

        assertThat(driver.findElement(By.cssSelector("h2")).getText()).isEqualTo(yesHeaderText);
    }

    @Test
    void shouldUseDefaultValueWhenDataForDatasourcePageIsNotPresent() {
        driver.navigate().to(baseUrl + "/pages/testStaticPage");

        assertThat(testPage.findElementTextByName("firstPage_someRadioInputName")).isEqualTo("default value");
    }
}
