package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-input.yaml"})
public class InputsPageTest extends AbstractExistingStartTimePageTest {

    String radioOption1 = "radio option 1";
    String radioOption2 = "option-2";
    String checkboxOption1 = "checkbox option 1";
    String checkboxOption2 = "checkbox option 2";
    String noneCheckboxOption = "none checkbox option";
    String selectOption1 = "select option 1";
    String selectOption2 = "select option 2";
    String followUpTrue = "YEP";
    String followUpFalse = "NOPE";
    String followUpUncertain = "UNSURE";
    String promptMessage = "prompt message";
    String helpMessage = "help message";
    String optionHelpMessage = "option help message";

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, "firstPageTitle");
        staticMessageSource.addMessage("next-page-title", Locale.ENGLISH, "nextPageTitle");
        staticMessageSource.addMessage("radio-option-1", Locale.ENGLISH, radioOption1);
        staticMessageSource.addMessage("radio-option-2", Locale.ENGLISH, radioOption2);
        staticMessageSource.addMessage("checkbox-option-1", Locale.ENGLISH, checkboxOption1);
        staticMessageSource.addMessage("checkbox-option-2", Locale.ENGLISH, checkboxOption2);
        staticMessageSource.addMessage("none-checkbox-option", Locale.ENGLISH, noneCheckboxOption);
        staticMessageSource.addMessage("select-option-1", Locale.ENGLISH, selectOption1);
        staticMessageSource.addMessage("select-option-2", Locale.ENGLISH, selectOption2);
        staticMessageSource.addMessage("follow-up-true", Locale.ENGLISH, followUpTrue);
        staticMessageSource.addMessage("follow-up-false", Locale.ENGLISH, followUpFalse);
        staticMessageSource.addMessage("follow-up-uncertain", Locale.ENGLISH, followUpUncertain);
        staticMessageSource.addMessage("prompt-message-key", Locale.ENGLISH, promptMessage);
        staticMessageSource.addMessage("help-message-key", Locale.ENGLISH, helpMessage);
        staticMessageSource.addMessage("option-help-key", Locale.ENGLISH, optionHelpMessage);
        staticMessageSource.addMessage("general.month", Locale.ENGLISH, "month");
        staticMessageSource.addMessage("general.day", Locale.ENGLISH, "day");
        staticMessageSource.addMessage("general.year", Locale.ENGLISH, "year");
    }

    @Test
    void shouldShowPromptAndHelpMessagesForInput() {
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
    void shouldShowHelpMessageKeyOnCheckboxOptions() {
        driver.navigate().to(baseUrl + "/pages/firstPage");

        assertThat(testPage.driver.findElementByClassName("checkbox").getText()).contains(optionHelpMessage);
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

    @Test
    void shouldHaveAccessToDatasources() {
        navigateTo("firstPage");
        String datasourceText = "Datasource Text";
        testPage.enter("editableTextInput", datasourceText);
        testPage.clickContinue();

        navigateTo("subworkflowPage");

        testPage.enter("value1", "a");
        testPage.clickContinue();
        testPage.enter("value1", "b");
        testPage.clickContinue();
        testPage.enter("value1", "c");
        testPage.clickContinue();

        navigateTo("pageWithReferenceCheckboxes");

        assertThat(testPage.findElementTextByName("iteration0")).isEqualTo("a");
        assertThat(testPage.findElementTextByName("iteration1")).isEqualTo("b");
        assertThat(testPage.findElementTextByName("iteration2")).isEqualTo("c");
        assertThat(testPage.findElementTextByName("datasourceText")).isEqualTo(datasourceText);
    }
}
