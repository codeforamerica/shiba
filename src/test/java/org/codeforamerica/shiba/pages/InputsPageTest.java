package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class InputsPageTest extends AbstractExistingStartTimePageTest {
    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-input.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-input")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    String radioOption1 = "radio option 1";
    String checkboxOption1 = "checkbox option 1";
    String checkboxOption2 = "checkbox option 2";
    String noneCheckboxOption = "none checkbox option";
    String selectOption1 = "select option 1";
    String followUpTrue = "YEP";
    String followUpFalse = "NOPE";
    String followUpUncertain = "UNSURE";

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.US, "firstPageTitle");
        staticMessageSource.addMessage("next-page-title", Locale.US, "nextPageTitle");
        staticMessageSource.addMessage("radio-option-1", Locale.US, radioOption1);
        staticMessageSource.addMessage("checkbox-option-1", Locale.US, checkboxOption1);
        staticMessageSource.addMessage("checkbox-option-2", Locale.US, checkboxOption2);
        staticMessageSource.addMessage("none-checkbox-option", Locale.US, noneCheckboxOption);
        staticMessageSource.addMessage("select-option-1", Locale.US, selectOption1);
        staticMessageSource.addMessage("follow-up-true", Locale.US, followUpTrue);
        staticMessageSource.addMessage("follow-up-false", Locale.US, followUpFalse);
        staticMessageSource.addMessage("follow-up-uncertain", Locale.US, followUpUncertain);
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
        testPage.enter("editableTextInput", textInputValue);

        String dateMonth = "10";
        String dateDay = "02";
        String dateYear = "1823";
        testPage.enter("dateInput", String.join("/", dateMonth, dateDay, dateYear));

        String numberInputValue = "11";
        testPage.enter("numberInput", numberInputValue);

        testPage.enter("radioInput", radioOption1);
        testPage.enter("checkboxInput", checkboxOption1);
        testPage.enter("checkboxInput", checkboxOption2);
        testPage.enter("selectInput", selectOption1);
        String moneyInputValue = "some money";
        testPage.enter("moneyInput", moneyInputValue);
        String hourlyWageValue = "some wage";
        testPage.enter("hourlyWageInput", hourlyWageValue);
        String incremeValue = "5";
        testPage.enter("increme", incremeValue);

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
        assertThat(testPage.getInputValue("hourlyWageInput")).isEqualTo(hourlyWageValue);
        assertThat(testPage.getInputValue("increme")).isEqualTo(incremeValue);
    }

    @Test
    void incrementerShouldUseForMaxMinAndDefaultValue() {
        navigateTo("firstPage");
        assertThat(testPage.getInputValue("increme")).isEqualTo("4");

        driver.findElement(By.className("incrementer__add")).click();
        assertThat(testPage.getInputValue("increme")).isEqualTo("5");
        driver.findElement(By.className("incrementer__add")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("5");

        driver.findElement(By.className("incrementer__subtract")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("4");
        driver.findElement(By.className("incrementer__subtract")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("3");
        driver.findElement(By.className("incrementer__subtract")).click();
        assertThat(testPage.getInputValue("incrementerInput")).isEqualTo("3");
    }

    @Nested
    class FollowUps {
        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldNotDisplayFollowUpQuestionsWhenFollowUpValueIsNotSelected(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpTrue);

            assertThat(driver.findElement(By.cssSelector(String.format("input[name^='%s-followUpTextInput']", inputName))).isDisplayed()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldDisplayFollowUpQuestionsWhenFollowUpValueIsSelected(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpFalse);

            assertThat(driver.findElement(By.cssSelector(String.format("input[name^='%s-followUpTextInput']", inputName))).isDisplayed()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldPreserveAnswerToFollowUpQuestions(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpFalse);
            String followUpTextInputValue = "some follow up";
            String followUpInputName = String.format("%s-followUpTextInput", inputName);
            testPage.enter(followUpInputName, followUpTextInputValue);

            testPage.clickContinue();
            testPage.goBack();

            assertThat(driver.findElement(By.cssSelector(String.format("input[name^='%s-followUpTextInput']", inputName))).isDisplayed()).isTrue();
            assertThat(testPage.getInputValue(followUpInputName)).isEqualTo(followUpTextInputValue);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "radioInputWithFollowUps",
                "checkboxInputWithFollowUps",
        })
        void shouldDisplayFollowUpQuestionsWhenAnyFollowUpValueIsSelected(String inputName) {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter(inputName, followUpUncertain);

            assertThat(driver.findElement(By.cssSelector(String.format("input[name^='%s-followUpTextInput']", inputName))).isDisplayed()).isTrue();
        }

        @Test
        void shouldContinueDisplayingFollowUpQuestionsWhenAFollowUpValueIsStillSelected() {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            testPage.enter("checkboxInputWithFollowUps", followUpFalse);
            testPage.enter("checkboxInputWithFollowUps", followUpUncertain);
            testPage.enter("checkboxInputWithFollowUps", followUpUncertain);

            assertThat(driver.findElement(By.cssSelector("input[name^='checkboxInputWithFollowUps-followUpTextInput']")).isDisplayed()).isTrue();
        }
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

        testPage.enter("checkboxInput", checkboxOption1);
        testPage.enter("checkboxInput", checkboxOption2);
        testPage.enter("checkboxInput", noneCheckboxOption);

        assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(noneCheckboxOption);
    }

    @Test
    void shouldUncheckNoneCheckboxWhenAnyOtherCheckboxIsSelected() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        testPage.enter("checkboxInput", noneCheckboxOption);
        testPage.enter("checkboxInput", checkboxOption1);

        assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(checkboxOption1);
    }

    @Test
    void shouldNotDisplayPrimaryButtonWhenHasPrimaryButtonIsFalse() {
        navigateTo("doNotHavePrimaryButtonPage");

        assertThat(driver.findElements(By.className("button--primary"))).isEmpty();
    }

    @Test
    void shouldDisplayFragmentForPage() {
        navigateTo("pageWithContextFragment");

        assertThat(driver.findElement(By.id("pageContext")).getText()).isEqualTo("this is context");
    }
}
