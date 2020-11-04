package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
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

public class PageDatasourcePageTest extends AbstractExistingStartTimePageTest {
    private final String staticPageWithDatasourceInputsTitle = "staticPageWithDatasourceInputsTitle";
    private final String yesHeaderText = "yes header text";

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-page-datasources.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-page-datasource")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, "firstPageTitle");
        staticMessageSource.addMessage("static-page-with-datasource-inputs-title", Locale.US, staticPageWithDatasourceInputsTitle);
        staticMessageSource.addMessage("yes-header-text", Locale.US, yesHeaderText);
        staticMessageSource.addMessage("general.inputs.yes", Locale.US, YesNoAnswer.YES.getDisplayValue());
        staticMessageSource.addMessage("general.inputs.no", Locale.US, YesNoAnswer.NO.getDisplayValue());
        staticMessageSource.addMessage("some-other-header", Locale.US, "some other header");
        staticMessageSource.addMessage("some-header", Locale.US, "some other header");
        staticMessageSource.addMessage("radio-value-key-1", Locale.US, "radio value 1");
        staticMessageSource.addMessage("radio-value-key-2", Locale.US, "radio value 2");
    }

    @Test
    void shouldDisplayDataEnteredFromAPreviousPage() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        String inputText = "some input";
        testPage.enter("someInputName", inputText);
        testPage.clickContinue();

        assertThat(testPage.findElementTextByName("someInputName")).isEqualTo(inputText);
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
    void shouldDisplayDatasourceForFormPages() {
        navigateTo("firstPage");

        String value = "some input value";
        testPage.enter("someInputName", value);
        testPage.clickContinue();

        navigateTo("testFormPage");
        assertThat(driver.findElement(By.id("context-fragment")).getText()).isEqualTo(value);
    }
}
