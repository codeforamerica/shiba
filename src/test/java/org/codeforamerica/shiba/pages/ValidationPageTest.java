package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

public class ValidationPageTest extends AbstractStaticMessageSourcePageTest {

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
    private final String incrementerPageTitle = "incrementer page title";
    private final String option1 = "option 1";

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-validation.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration extends MetricsTestConfigurationWithExistingStartTime {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-test-validation")
        public PagesConfiguration pagesConfiguration() {
            return new PagesConfiguration();
        }
    }

    Page testPage;

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        testPage = new Page(driver);
        staticMessageSource.addMessage("first-page-title", Locale.US, firstPageTitle);
        staticMessageSource.addMessage("next-page-title", Locale.US, nextPageTitle);
        staticMessageSource.addMessage("last-page-title", Locale.US, lastPageTitle);
        staticMessageSource.addMessage("error-message-key", Locale.US, errorMessage);
        staticMessageSource.addMessage("zip-code-page-title", Locale.US, zipcodePageTitle);
        staticMessageSource.addMessage("state-page-title", Locale.US, statePageTitle);
        staticMessageSource.addMessage("phone-page-title", Locale.US, phonePageTitle);
        staticMessageSource.addMessage("money-page-title", Locale.US, moneyPageTitle);
        staticMessageSource.addMessage("ssn-page-title", Locale.US, ssnPageTitle);
        staticMessageSource.addMessage("date-page-title", Locale.US, datePageTitle);
        staticMessageSource.addMessage("not-blank-page-title", Locale.US, notBlankPageTitle);
        staticMessageSource.addMessage("checkbox-page-title", Locale.US, checkboxPageTitle);
        staticMessageSource.addMessage("incrementer-page-title", Locale.US, incrementerPageTitle);
        staticMessageSource.addMessage("option-1", Locale.US, option1);
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
        testPage.clickPrimaryButton();

        assertThat(testPage.hasInputError("someInputName")).isTrue();

        testPage.enterInput("someInputName", "not blank");
        testPage.clickPrimaryButton();
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

    @Nested
    class Condition {
        @Test
        void shouldTriggerValidation_whenConditionInputValueIsNotPresent() {
            driver.navigate().to(baseUrl + "/pages/firstPage");
            driver.findElement(By.cssSelector("input[name^='someInputName']")).sendKeys("do not trigger validation");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(nextPageTitle);

            driver.findElement(By.cssSelector("button")).click();

            assertThat(testPage.getInputError("conditionalValidationWhenValueIsNotPresent")).isNotNull();
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

        @Test
        void shouldNotTriggerValidation_whenConditionInputContainsValue() {
            navigateTo("doesNotContainConditionPage");

            testPage.enterInput("triggerInput", "triggerValue");
            testPage.clickPrimaryButton();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldTriggerValidation_whenConditionInputDoesNotContainValue() {
            navigateTo("doesNotContainConditionPage");

            testPage.enterInput("triggerInput", "not trigger");
            testPage.clickPrimaryButton();

            assertThat(testPage.hasInputError("conditionTest")).isTrue();
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
            testPage.enterInput("notBlankInput", textInputValue);

            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(notBlankPageTitle);
        }

        @Test
        void shouldPassValidationForNOT_BLANKWhenThereIsAtLeast1CharacterInput() {
            driver.navigate().to(baseUrl + "/pages/notBlankPage");
            testPage.enterInput("notBlankInput", "something");

            testPage.clickPrimaryButton();
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
        void shouldPassValidationForStateWhenValueIsAKnownStateCode() {
            driver.navigate().to(baseUrl + "/pages/statePage");
            driver.findElement(By.cssSelector("input[name^='stateInput']")).sendKeys("XY");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(statePageTitle);
            assertThat(testPage.getInputError("stateInput")).isNotNull();

            driver.findElement(By.cssSelector("input[name^='stateInput']")).clear();
            driver.findElement(By.cssSelector("input[name^='stateInput']")).sendKeys("MN");
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

        @Test
        void shouldFailValidationForMoneyWhenValueIsNotNumber() {
            driver.navigate().to(baseUrl + "/pages/moneyPage");
            driver.findElement(By.cssSelector("input[name^='moneyInput']")).sendKeys("a123");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(moneyPageTitle);
            assertThat(testPage.getInputError("moneyInput")).isNotNull();
        }

        @Test
        void shouldPassValidationForMoneyWhenValueIsNumber() {
            driver.navigate().to(baseUrl + "/pages/moneyPage");
            driver.findElement(By.cssSelector("input[name^='moneyInput']")).sendKeys("-726");
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
                "1,2,1929",
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

        @Test
        void shouldPassValidationForDateWhenValueIsAValidDate() {
            driver.navigate().to(baseUrl + "/pages/datePage");

            driver.findElements(By.cssSelector("input[name^='dateInput']")).forEach(WebElement::clear);

            driver.findElement(By.id("dateInput-month")).sendKeys("02");
            driver.findElement(By.id("dateInput-day")).sendKeys("20");
            driver.findElement(By.id("dateInput-year")).sendKeys("1492");
            driver.findElement(By.cssSelector("button")).click();

            assertThat(driver.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldFailValidationForSELECT_AT_LEAST_ONEWhenNoValuesAreSelected() {
            driver.navigate().to(baseUrl + "/pages/checkboxPage");

            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(checkboxPageTitle);
        }

        @Test
        void shouldPassValidationForSELECT_AT_LEAST_ONEWhenAtLeastOneValueIsSelected() {
            driver.navigate().to(baseUrl + "/pages/checkboxPage");

            testPage.selectEnumeratedInput("checkboxInput", option1);
            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldFailValidationForNUMBER_OF_JOBSWhenValueIsLessThan0() {
            navigateTo("incrementerPage");
            testPage.enterInput("incrementerInput", "-1");

            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(incrementerPageTitle);
        }

        @Test
        void shouldFailValidationForNUMBER_OF_JOBSWhenValueIsGreaterThan20() {
            navigateTo("incrementerPage");
            testPage.enterInput("incrementerInput", "21");

            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(incrementerPageTitle);
        }

        @Test
        void shouldFailValidationForNUMBER_OF_JOBSWhenValueIsEmpty() {
            navigateTo("incrementerPage");
            testPage.enterInput("incrementerInput", "  ");

            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(incrementerPageTitle);
        }

        @Test
        void shouldFailValidationForNUMBER_OF_JOBSWhenValueContainsAlphaCharacters() {
            navigateTo("incrementerPage");
            testPage.enterInput("incrementerInput", "1e1");

            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(incrementerPageTitle);
        }

        @Test
        void shouldPassValidationForNUMBER_OF_JOBSWhenValueIsBetween0and20() {
            navigateTo("incrementerPage");
            testPage.enterInput("incrementerInput", "19");

            testPage.clickPrimaryButton();
            assertThat(testPage.getTitle()).isEqualTo(lastPageTitle);
        }
    }

}
