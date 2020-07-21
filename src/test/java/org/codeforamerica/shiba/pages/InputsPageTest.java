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

public class InputsPageTest extends AbstractStaticMessageSourcePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-input.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-input")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    String radioOption1 = "radio option 1";
    String checkboxOption1 = "checkbox option 1";
    String checkboxOption2 = "checkbox option 2";
    String noneCheckboxOption = "none checkbox option";
    String selectOption1 = "select option 1";
    String radioTrue = "YEP";
    String radioFalse = "NOPE";

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, "firstPageTitle");
        staticMessageSource.addMessage("next-page-title", Locale.US, "nextPageTitle");
        staticMessageSource.addMessage("radio-option-1", Locale.US, radioOption1);
        staticMessageSource.addMessage("checkbox-option-1", Locale.US, checkboxOption1);
        staticMessageSource.addMessage("checkbox-option-2", Locale.US, checkboxOption2);
        staticMessageSource.addMessage("none-checkbox-option", Locale.US, noneCheckboxOption);
        staticMessageSource.addMessage("select-option-1", Locale.US, selectOption1);
        staticMessageSource.addMessage("radio-true", Locale.US, radioTrue);
        staticMessageSource.addMessage("radio-false", Locale.US, radioFalse);
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

        String textInputValue = "some input";
        testPage.enterInput("editableTextInput", textInputValue);

        String dateMonth = "10";
        String dateDay = "02";
        String dateYear = "1823";
        testPage.enterDateInput("dateInput", DatePart.MONTH, dateMonth);
        testPage.enterDateInput("dateInput", DatePart.DAY, dateDay);
        testPage.enterDateInput("dateInput", DatePart.YEAR, dateYear);

        String numberInputValue = "11";
        testPage.enterInput("numberInput", numberInputValue);

        testPage.selectEnumeratedInput("radioInput", radioOption1);
        testPage.selectEnumeratedInput("checkboxInput", checkboxOption1);
        testPage.selectEnumeratedInput("checkboxInput", checkboxOption2);
        testPage.selectFromDropdown("selectInput", selectOption1);
        String moneyInputValue = "some money";
        testPage.enterInput("moneyInput", moneyInputValue);
        String incrementerInputValue = "5";
        testPage.enterInput("incrementerInput", incrementerInputValue);

        driver.findElement(By.cssSelector("button")).click();
        assertThat(driver.getTitle()).isEqualTo("nextPageTitle");
        driver.findElement(By.partialLinkText("Go Back")).click();
        assertThat(driver.getTitle()).isEqualTo("firstPageTitle");

        assertThat(testPage.getInputValue("editableTextInput")).isEqualTo(textInputValue);
        assertThat(testPage.getBirthDateValue("dateInput", DatePart.MONTH)).isEqualTo(dateMonth);
        assertThat(testPage.getBirthDateValue("dateInput", DatePart.DAY)).isEqualTo(dateDay);
        assertThat(testPage.getBirthDateValue("dateInput", DatePart.YEAR)).isEqualTo(dateYear);
        assertThat(testPage.getInputValue("numberInput")).isEqualTo(numberInputValue);
        assertThat(testPage.getRadioValue("radioInput")).isEqualTo(radioOption1);
        assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(checkboxOption1, checkboxOption2);
        assertThat(testPage.getSelectValue("selectInput")).isEqualTo(selectOption1);
        assertThat(testPage.getInputValue("moneyInput")).isEqualTo(moneyInputValue);
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo(incrementerInputValue);
    }

    @Test
    void incrementerShouldUseForMaxMinAndDefaultValue() {
        navigateTo("firstPage");
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("4");

        driver.findElement(By.className("incrementer__add")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("5");
        driver.findElement(By.className("incrementer__add")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("5");

        driver.findElement(By.className("incrementer__subtract")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("4");
        driver.findElement(By.className("incrementer__subtract")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("3");
        driver.findElement(By.className("incrementer__subtract")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("3");
    }

    @Test
    void shouldNotDisplayFollowUpQuestionsWhenFollowUpValueIsNotSelected() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        testPage.selectEnumeratedInput("inputWithFollowUps", radioTrue);

        assertThat(driver.findElement(By.cssSelector("input[name^='followUpTextInput']")).isDisplayed()).isFalse();
    }

    @Test
    void shouldDisplayFollowUpQuestionsWhenFollowUpValueIsSelected() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        testPage.selectEnumeratedInput("inputWithFollowUps", radioFalse);

        assertThat(driver.findElement(By.cssSelector("input[name^='followUpTextInput']")).isDisplayed()).isTrue();
    }

    @Test
    void shouldPreserveAnswerToFollowUpQuestions() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        testPage.selectEnumeratedInput("inputWithFollowUps", radioFalse);
        String followUpTextInputValue = "some follow up";
        testPage.enterInput("followUpTextInput", followUpTextInputValue);

        testPage.clickPrimaryButton();
        testPage.goBack();

        assertThat(testPage.getInputValue("followUpTextInput")).isEqualTo(followUpTextInputValue);
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

    @Test
    void shouldDisplayPromptMessageFragment() {
        driver.navigate().to(baseUrl + "/pages/inputWithPromptFragmentPage");

        assertThat(driver.findElementByPartialLinkText("test message"));
    }

    @Test
    void shouldUncheckAnyOtherCheckedBoxesWhenNoneCheckboxIsSelected() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        testPage.selectEnumeratedInput("checkboxInput", checkboxOption1);
        testPage.selectEnumeratedInput("checkboxInput", checkboxOption2);
        testPage.selectEnumeratedInput("checkboxInput", noneCheckboxOption);

        assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(noneCheckboxOption);
    }

    @Test
    void shouldUncheckNoneCheckboxWhenAnyOtherCheckboxIsSelected() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        testPage.selectEnumeratedInput("checkboxInput", noneCheckboxOption);
        testPage.selectEnumeratedInput("checkboxInput", checkboxOption1);

        assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(checkboxOption1);
    }
}
