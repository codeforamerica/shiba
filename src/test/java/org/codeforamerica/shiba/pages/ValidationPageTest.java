package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/test-validation.yaml"
})
public class ValidationPageTest extends AbstractExistingStartTimePageTest {

    private final String errorMessage = "error message";
    private final String nextPageTitle = "next Page Title";
    private final String lastPageTitle = "last page title";
    private final String firstPageTitle = "first Page Title";
    private final String zipcodePageTitle = "zip code Page Title";
    private final String statePageTitle = "state page title";
    private final String phonePageTitle = "phone page title";
    private final String moneyPageTitle = "money page title";
    private final String ssnPageTitle = "ssn page title";
    private final String datePageTitle = "date page title";
    private final String notBlankPageTitle = "not blank page title";
    private final String checkboxPageTitle = "checkbox page title";
    private final String option1 = "option 1";
    private final String multipleValidationsPageTitle = "multiple validations page title";
    private final String moneyErrorMessageKey = "money is error";
    private final String emailPageTitle = "email page title";

    Page testPage;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        testPage = new Page(driver);
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("next-page-title", Locale.ENGLISH, nextPageTitle);
        staticMessageSource.addMessage("last-page-title", Locale.ENGLISH, lastPageTitle);
        staticMessageSource.addMessage("error-message-key", Locale.ENGLISH, errorMessage);
        staticMessageSource.addMessage("money-error-message-key", Locale.ENGLISH, moneyErrorMessageKey);
        staticMessageSource.addMessage("not-blank-error-message-key ", Locale.ENGLISH, "not blank is error");
        staticMessageSource.addMessage("zip-code-page-title", Locale.ENGLISH, zipcodePageTitle);
        staticMessageSource.addMessage("state-page-title", Locale.ENGLISH, statePageTitle);
        staticMessageSource.addMessage("phone-page-title", Locale.ENGLISH, phonePageTitle);
        staticMessageSource.addMessage("money-page-title", Locale.ENGLISH, moneyPageTitle);
        staticMessageSource.addMessage("ssn-page-title", Locale.ENGLISH, ssnPageTitle);
        staticMessageSource.addMessage("date-page-title", Locale.ENGLISH, datePageTitle);
        staticMessageSource.addMessage("email-page-title", Locale.ENGLISH, emailPageTitle);
        staticMessageSource.addMessage("not-blank-page-title", Locale.ENGLISH, notBlankPageTitle);
        staticMessageSource.addMessage("checkbox-page-title", Locale.ENGLISH, checkboxPageTitle);
        staticMessageSource.addMessage("option-1", Locale.ENGLISH, option1);
        staticMessageSource.addMessage("option-2", Locale.ENGLISH, "option-2");
        staticMessageSource.addMessage("page-with-input-with-multiple-validations", Locale.ENGLISH, multipleValidationsPageTitle);
    }

    @Test
    void shouldStayOnThePage_whenValidationFails() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldGoOnToNextPage_whenValidationPasses() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("something");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(nextPageTitle);
    }

    @Test
    void shouldClearValidationError_afterErrorHasBeenFixed() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        testPage.clickContinue();

        assertThat(testPage.hasInputError("someInputName")).isTrue();

        testPage.enter("someInputName", "not blank");
        testPage.clickContinue();
        testPage.goBack();

        assertThat(testPage.hasInputError("someInputName")).isFalse();
    }

    @Test
    void shouldDisplayErrorMessageWhenValidationFailed() {
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(testPage.getInputError("someInputName").getText()).isEqualTo(errorMessage);
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

        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);
        assertThat(testPage.getInputError("conditionalValidationWhenValueEquals")).isNotNull();
    }

    @Test
    void shouldStayOnPage_whenAnyValidationHasFailed() {
        navigateTo("pageWithInputWithMultipleValidations");

        testPage.enter("multipleValidations", "not money");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo(multipleValidationsPageTitle);
        assertThat(testPage.getInputError("multipleValidations").getText()).isEqualTo(moneyErrorMessageKey);
    }

    @Nested
    class Condition {
        @Test
        void shouldTriggerValidation_whenConditionInputValueIsNoneSelected() {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("do not trigger validation");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(nextPageTitle);

            driver.findElement(By.cssSelector("button")).click();

            assertThat(testPage.getInputError("conditionalValidationWhenValueIsNoneSelected")).isNotNull();
        }

        @Test
        void shouldNotTriggerValidation_whenConditionInputValueIsSelected() {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("do not trigger validation");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(nextPageTitle);
            driver.findElement(By.cssSelector("input[name^='someCheckbox']")).click();
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldNotTriggerValidation_whenConditionInputContainsValue() {
            navigateTo("doesNotContainConditionPage");

            testPage.enter("triggerInput", "triggerValue");
            testPage.clickContinue();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldTriggerValidation_whenConditionInputDoesNotContainValue() {
            navigateTo("doesNotContainConditionPage");

            testPage.enter("triggerInput", "not trigger");
            testPage.clickContinue();

            assertThat(testPage.hasInputError("conditionTest")).isTrue();
        }

        @Test
        void shouldTriggerValidation_whenConditionInputIsEmptyOrBlank() {
            navigateTo("emptyInputConditionPage");

            testPage.enter("triggerInput", "");
            testPage.clickContinue();

            assertThat(driver.getTitle()).isNotEqualTo(lastPageTitle);
            assertThat(testPage.hasInputError("conditionTest")).isTrue();
        }

        @Test
        void shouldNotTriggerValidation_whenConditionInputIsNotEmptyOrBlank() {
            navigateTo("emptyInputConditionPage");

            testPage.enter("triggerInput", "something");
            testPage.clickContinue();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }
    }

    @Nested
    class SpecificValidations {
        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   "
        })
        void shouldFailValidationForNOT_BLANKWhenThereIsEmptyOrBlankInput(String textInputValue) {
            driver.navigate().to(baseUrl + "/pages/notBlankPage");
            testPage.enter("notBlankInput", textInputValue);

            testPage.clickContinue();
            assertThat(testPage.getTitle()).isEqualTo(notBlankPageTitle);
        }

        @Test
        void shouldPassValidationForNOT_BLANKWhenThereIsAtLeast1CharacterInput() {
            driver.navigate().to(baseUrl + "/pages/notBlankPage");
            testPage.enter("notBlankInput", "something");

            testPage.clickContinue();
            assertThat(testPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "123456",
                "1234",
                "1234e"
        })
        void shouldFailValidationForZipCodeWhenValueIsNotExactlyFiveDigits(String input) {
            driver.navigate().to(baseUrl + "/pages/zipcodePage");
            driver.findElement(By.cssSelector("input[name^='zipCodeInput']")).sendKeys(input);
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(zipcodePageTitle);
            assertThat(testPage.getInputError("zipCodeInput")).isNotNull();
        }

        @Test
        void shouldPassValidationForZipCodeWhenValueIsExactlyFiveDigits() {
            driver.navigate().to(baseUrl + "/pages/zipcodePage");

            driver.findElement(By.cssSelector("input[name^='zipCodeInput']")).clear();
            driver.findElement(By.cssSelector("input[name^='zipCodeInput']")).sendKeys("12345");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldPassValidationForStateWhenValueIsAKnownStateCode_caseInsensitive() {
            driver.navigate().to(baseUrl + "/pages/statePage");
            driver.findElement(By.cssSelector("input[name^='stateInput']")).sendKeys("XY");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(statePageTitle);
            assertThat(testPage.getInputError("stateInput")).isNotNull();

            driver.findElement(By.cssSelector("input[name^='stateInput']")).clear();
            driver.findElement(By.cssSelector("input[name^='stateInput']")).sendKeys("mn");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "723456789",
                "72345678901",
                "723456789e",
                "723-456789",
                "1234567890",
                "0234567890",
        })
        void shouldFailValidationForPhoneIfValueIsNotExactly10DigitsOrStartsWithAZeroOrOne(String input) {
            driver.navigate().to(baseUrl + "/pages/phonePage");
            driver.findElement(By.cssSelector("input[name^='phoneInput']")).sendKeys(input);
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(phonePageTitle);
            assertThat(testPage.getInputError("phoneInput")).isNotNull();
        }

        @Test
        void shouldPassValidationForPhoneIfAndOnlyIfValueIsExactly10Digits() {
            driver.navigate().to(baseUrl + "/pages/phonePage");
            driver.findElement(By.cssSelector("input[name^='phoneInput']")).sendKeys("7234567890");

            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "a123",
                "1.",
                "51.787",
                "1.000",
                "-152"
        })
        void shouldFailValidationForMoneyWhenValueIsNotAWholeDollarAmount(String value) {
            driver.navigate().to(baseUrl + "/pages/moneyPage");
            driver.findElement(By.cssSelector("input[name^='moneyInput']")).sendKeys(value);
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(moneyPageTitle);
            assertThat(testPage.getInputError("moneyInput")).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "14",
                "1.1",
                "16.71"
        })
        void shouldPassValidationForMoneyWhenValueIsAPositiveWholeDollarAmount(String value) {
            driver.navigate().to(baseUrl + "/pages/moneyPage");
            driver.findElement(By.cssSelector("input[name^='moneyInput']")).sendKeys(value);
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1234567890",
                "12345678",
                "12345678e"
        })
        void shouldFailValidationForSSNWhenValueIsNotExactlyNineDigits(String input) {
            driver.navigate().to(baseUrl + "/pages/ssnPage");
            driver.findElement(By.cssSelector("input[name^='ssnInput']")).sendKeys(input);
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(ssnPageTitle);
            assertThat(testPage.getInputError("ssnInput")).isNotNull();
        }

        @Test
        void shouldPassValidationForSSNWhenValueIsExactlyNineDigits() {
            driver.navigate().to(baseUrl + "/pages/ssnPage");

            driver.findElement(By.cssSelector("input[name^='ssnInput']")).clear();
            driver.findElement(By.cssSelector("input[name^='ssnInput']")).sendKeys("123456789");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @CsvSource(value = {
                "13,42,1492",
                "0,2,1929",
                "1,2,929",
        })
        void shouldFailValidationForDateWhenValueIsNotAValidDate(String month,
                                                                 String day,
                                                                 String year) {
            driver.navigate().to(baseUrl + "/pages/datePage");
            driver.findElements(By.cssSelector("input[name^='dateInput']")).forEach(WebElement::clear);

            driver.findElement(By.id("dateInput-month")).sendKeys(month);
            driver.findElement(By.id("dateInput-day")).sendKeys(day);
            driver.findElement(By.id("dateInput-year")).sendKeys(year);
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(datePageTitle);
            assertThat(testPage.getInputError("dateInput")).isNotNull();
        }

        @ParameterizedTest
        @CsvSource(value = {
                "02,20,1492",
                "1,2,1929",
        })
        void shouldPassValidationForDateWhenValueIsAValidDate(String month,
                                                              String day,
                                                              String year) {
            driver.navigate().to(baseUrl + "/pages/datePage");

            driver.findElements(By.cssSelector("input[name^='dateInput']")).forEach(WebElement::clear);

            driver.findElement(By.id("dateInput-month")).sendKeys(month);
            driver.findElement(By.id("dateInput-day")).sendKeys(day);
            driver.findElement(By.id("dateInput-year")).sendKeys(year);
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldFailValidationForSELECT_AT_LEAST_ONEWhenNoValuesAreSelected() {
            driver.navigate().to(baseUrl + "/pages/checkboxPage");

            testPage.clickContinue();
            assertThat(testPage.getTitle()).isEqualTo(checkboxPageTitle);
        }

        @Test
        void shouldPassValidationForSELECT_AT_LEAST_ONEWhenAtLeastOneValueIsSelected() {
            driver.navigate().to(baseUrl + "/pages/checkboxPage");

            testPage.enter("checkboxInput", option1);
            testPage.clickContinue();
            assertThat(testPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @CsvSource(value = {
                "asdf, " + emailPageTitle,
                "almost@.com, " + emailPageTitle,
                "\" \" , " + emailPageTitle,
                "fake@test.com, " + lastPageTitle,
                "FAKE@TEST.COM, " + lastPageTitle
        })
        void shouldPassValidationForEMAILWhenThereIsAValidEmail(String emailInput, String expectedPage) {
            driver.navigate().to(baseUrl + "/pages/pageWithEmail");
            testPage.enter("emailInput", emailInput);

            testPage.clickContinue();
            assertThat(testPage.getTitle()).isEqualTo(expectedPage);
        }

        @Test
        void shouldPassValidationForEMAILWhenValidEmailHasTrailingWhitespace() {
            driver.navigate().to(baseUrl + "/pages/pageWithEmail");
            testPage.enter("emailInput", "fake@test.com ");

            testPage.clickContinue();
            assertThat(testPage.getTitle()).isEqualTo(lastPageTitle);
        }
    }

}
