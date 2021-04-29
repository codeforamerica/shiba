package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-page-datasources.yaml"})
public class PageDatasourcePageTest extends AbstractExistingStartTimePageTest {
    private final String staticPageWithDatasourceInputsTitle = "staticPageWithDatasourceInputsTitle";
    private final String yesHeaderText = "yes header text";
    private final String noHeaderText = "no header text";
    private final String noAnswerTitle = "no answer title";

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, "firstPageTitle");
        staticMessageSource.addMessage("static-page-with-datasource-inputs-title", Locale.ENGLISH, staticPageWithDatasourceInputsTitle);
        staticMessageSource.addMessage("yes-header-text", Locale.ENGLISH, yesHeaderText);
        staticMessageSource.addMessage("no-header-text", Locale.ENGLISH, noHeaderText);
        staticMessageSource.addMessage("general.inputs.yes", Locale.ENGLISH, YesNoAnswer.YES.getDisplayValue());
        staticMessageSource.addMessage("general.inputs.no", Locale.ENGLISH, YesNoAnswer.NO.getDisplayValue());
        staticMessageSource.addMessage("some-other-header", Locale.ENGLISH, "some other header");
        staticMessageSource.addMessage("some-header", Locale.ENGLISH, "some other header");
        staticMessageSource.addMessage("no-answer-title", Locale.ENGLISH, noAnswerTitle);
        staticMessageSource.addMessage("radio-value-key-1", Locale.ENGLISH, "radio value 1");
        staticMessageSource.addMessage("radio-value-key-2", Locale.ENGLISH, "radio value 2");
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
        staticMessageSource.addMessage("foo", Locale.ENGLISH, "wrong title");
        staticMessageSource.addMessage("no-answer-title", Locale.ENGLISH, noAnswerTitle);

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

        assertThat(driver.findElement(By.cssSelector("h1")).getText()).isEqualTo(yesHeaderText);
    }

    @Test
    void shouldDisplayPageHeaderBasedOnCompositeCondition() {
        driver.navigate().to(baseUrl + "/pages/yesNoQuestionPage2");

        List<WebElement> yesNoRadios = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect = yesNoRadios.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.YES.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();

        List<WebElement> yesNoRadios2 = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect2 = yesNoRadios2.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.NO.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect2.click();

        assertThat(driver.findElement(By.cssSelector("h1")).getText()).isEqualTo(yesHeaderText);
    }

    @Test
    void shouldDisplayPageHeaderBasedOnCompositeConditionOtherPath() {
        driver.navigate().to(baseUrl + "/pages/yesNoQuestionPage2");

        List<WebElement> yesNoRadios = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect = yesNoRadios.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.NO.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();

        List<WebElement> yesNoRadios2 = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect2 = yesNoRadios2.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.YES.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect2.click();

        assertThat(driver.findElement(By.cssSelector("h1")).getText()).isEqualTo(noHeaderText);
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

    @Test
    void shouldGetDataFromDatasourcesOutsideOfSubworkflow() {
        driver.navigate().to(baseUrl + "/pages/outsideSubworkflowPage");

        List<WebElement> yesNoRadios = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect = yesNoRadios.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.YES.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();

        assertThat(driver.findElement(By.cssSelector("h1")).getText()).isEqualTo(yesHeaderText);
    }

    @Test
    void shouldHandleMissingDatasourcePagesWhenDatasourcePageWasSkipped() {
        driver.navigate().to(baseUrl + "/pages/outsideSubworkflowPage");

        List<WebElement> yesNoRadios = driver.findElements(By.cssSelector(".button"));
        WebElement radioToSelect = yesNoRadios.stream()
                .filter(label -> label.getText().equals(YesNoAnswer.YES.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();

        assertThat(driver.getTitle()).isEqualTo(noAnswerTitle);
    }
}
