package org.codeforamerica.shiba.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@Tag("validation")
@SpringBootTest(properties = {"pagesConfig=pages-config/test-validation.yaml"})
public class ValidationTest extends AbstractFrameworkTest {
    private final String errorMessage = "error message";
    private final String nextPageTitle = "next Page Title";
    private final String lastPageTitle = "last page title";
    private final String firstPageTitle = "first Page Title";
    private final String zipcodePageTitle = "zip code Page Title";
    private final String caseNumberPageTitle = "case number Page Title";
    private final String statePageTitle = "state page title";
    private final String phonePageTitle = "phone page title";
    private final String moneyPageTitle = "money page title";
    private final String numberPageTitle = "hours per week page title";
    private final String ssnPageTitle = "ssn page title";
    private final String datePageTitle = "date page title";
    private final String dobValidPageTitle = "dob valid page title";
    private final String notBlankPageTitle = "not blank page title";
    private final String checkboxPageTitle = "checkbox page title";
    private final String option1 = "option 1";
    private final String multipleValidationsPageTitle = "multiple validations page title";
    private final String moneyErrorMessageKey = "money is error";
    private final String emailPageTitle = "email page title";
    private final String selectCountyPageTitle = "select county page title";
    private final String selectCounty = "select your county";
    private final String countyA = "Alpha";
    private final String countyB = "Beta";

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("next-page-title", ENGLISH, nextPageTitle);
        staticMessageSource.addMessage("last-page-title", ENGLISH, lastPageTitle);
        staticMessageSource.addMessage("error-message-key", ENGLISH, errorMessage);
        staticMessageSource.addMessage("money-error-message-key", ENGLISH, moneyErrorMessageKey);
        staticMessageSource.addMessage("not-blank-error-message-key ", ENGLISH, "not blank is error");
        staticMessageSource.addMessage("zip-code-page-title", ENGLISH, zipcodePageTitle);
        staticMessageSource.addMessage("case-number-page-title", ENGLISH, caseNumberPageTitle);
        staticMessageSource.addMessage("state-page-title", ENGLISH, statePageTitle);
        staticMessageSource.addMessage("phone-page-title", ENGLISH, phonePageTitle);
        staticMessageSource.addMessage("money-page-title", ENGLISH, moneyPageTitle);
        staticMessageSource.addMessage("number-title", ENGLISH, numberPageTitle);
        staticMessageSource.addMessage("ssn-page-title", ENGLISH, ssnPageTitle);
        staticMessageSource.addMessage("date-page-title", ENGLISH, datePageTitle);
        staticMessageSource.addMessage("dob-valid-page-title", ENGLISH, dobValidPageTitle);
        staticMessageSource.addMessage("email-page-title", ENGLISH, emailPageTitle);
        staticMessageSource.addMessage("select-county-page-title", ENGLISH, selectCountyPageTitle);
        staticMessageSource.addMessage("not-blank-page-title", ENGLISH, notBlankPageTitle);
        staticMessageSource.addMessage("checkbox-page-title", ENGLISH, checkboxPageTitle);
        staticMessageSource.addMessage("option-1", ENGLISH, option1);
        staticMessageSource.addMessage("option-2", ENGLISH, "option-2");
        staticMessageSource.addMessage("page-with-input-with-multiple-validations",
                                       ENGLISH,
                                       multipleValidationsPageTitle);
        staticMessageSource.addMessage("select-county-key", ENGLISH, selectCounty);
        staticMessageSource.addMessage("county-a-key", ENGLISH, countyA);
        staticMessageSource.addMessage("county-b-key", ENGLISH, countyB);
    }

    @Test
    void shouldStayOnThePage_whenValidationFails() throws Exception {
        postWithoutData("firstPage").andExpect(redirectedUrl("/pages/firstPage"));
    }

    @Test
    void shouldGoOnToNextPage_whenValidationPasses() throws Exception {
        var page = postExpectingSuccessAndFollowRedirect("firstPage", "someInputName", "something");
        assertThat(page.getTitle()).isEqualTo(nextPageTitle);
    }

    @Test
    void shouldClearValidationError_afterErrorHasBeenFixed() throws Exception {
        var pageName = "firstPage";
        var inputName = "someInputName";

        // Submit the page without required fields filled out, should be kept on current page
        assertPageDoesNotHaveInputError(pageName, inputName);
        postWithoutData(pageName).andExpect(redirectedUrl("/pages/" + pageName));

        // Submit with required fields filled out this time
        assertPageHasInputError(pageName, inputName);
        postExpectingSuccess(pageName, inputName, "not blank");

        // When I hit the back button, no input error should be displayed
        assertPageDoesNotHaveInputError(pageName, inputName);
    }

    @Test
    void shouldDisplayErrorMessageWhenValidationFailed() throws Exception {
        assertPageDoesNotHaveInputError("firstPage", "someInputName");
        postWithoutData("firstPage").andExpect(redirectedUrl("/pages/firstPage"));

        var page = new FormPage(getPage("firstPage"));
        assertThat(page.getInputError("someInputName").text()).isEqualTo(errorMessage);
    }

    @Test
    void shouldNotTriggerValidation_whenConditionIsFalse() throws Exception {
        var page = postExpectingSuccessAndFollowRedirect("firstPage", "someInputName", "do not trigger validation");
        assertThat(page.getTitle()).isEqualTo(nextPageTitle);
    }

    @Test
    void shouldTriggerValidation_whenConditionIsTrue() throws Exception {
        assertPageDoesNotHaveInputError("firstPage", "someInputName");
        postAndAssertErrorDisplaysOnAnotherInput("firstPage", "someInputName", "valueToTriggerCondition",
                                                 "conditionalValidationWhenValueEquals");
    }

    @Test
    void shouldStayOnPage_whenAnyValidationHasFailed() throws Exception {
        postExpectingFailure("pageWithInputWithMultipleValidations", "multipleValidations", "not money");

        var page = new FormPage(getPage("pageWithInputWithMultipleValidations"));
        assertThat(page.getTitle()).isEqualTo(multipleValidationsPageTitle);
        assertThat(page.getInputError("multipleValidations").text()).isEqualTo(moneyErrorMessageKey);
    }


    @Nested
    @Tag("validation")
    class Condition {
        @Test
        void shouldTriggerValidation_whenConditionInputValueIsNoneSelected() throws Exception {
            var nextPage = postExpectingSuccessAndFollowRedirect("firstPage",
                                                                 "someInputName",
                                                                 "do not trigger validation");
            assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);

            postWithoutData("nextPage").andExpect(redirectedUrl("/pages/nextPage"));
            assertPageHasInputError("nextPage", "conditionalValidationWhenValueIsNoneSelected");
        }

        @Test
        void shouldNotTriggerValidation_whenConditionInputValueIsSelected() throws Exception {
            var nextPage = postExpectingSuccessAndFollowRedirect("firstPage",
                                                                 "someInputName",
                                                                 "do not trigger validation");
            assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);
            var lastPage = postExpectingSuccessAndFollowRedirect("nextPage",
                                                                 "someCheckbox",
                                                                 "VALUE_1");
            assertThat(lastPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldNotTriggerValidation_whenConditionInputContainsValue() throws Exception {
            var lastPage = postExpectingSuccessAndFollowRedirect("doesNotContainConditionPage",
                                                                 "triggerInput",
                                                                 "triggerValue");
            assertThat(lastPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldTriggerValidation_whenConditionInputDoesNotContainValue() throws Exception {
            postAndAssertErrorDisplaysOnAnotherInput("doesNotContainConditionPage", "triggerInput", "not trigger",
                                                     "conditionTest");
        }

        @Test
        void shouldTriggerValidation_whenConditionInputIsEmptyOrBlank() throws Exception {
            postAndAssertErrorDisplaysOnAnotherInput("emptyInputConditionPage", "triggerInput", "", "conditionTest");
        }

        @Test
        void shouldNotTriggerValidation_whenConditionInputIsNotEmptyOrBlank() throws Exception {
            var page = postExpectingSuccessAndFollowRedirect("emptyInputConditionPage",
                                                             "triggerInput",
                                                             "something");
            assertThat(page.getTitle()).isEqualTo(lastPageTitle);
        }
    }

    @Nested
    @Tag("validation")
    class SpecificValidations {
        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   "
        })
        void shouldFailValidationForNOT_BLANKWhenThereIsEmptyOrBlankInput(String textInputValue) throws Exception {
            postAndAssertInputErrorDisplays("notBlankPage", "notBlankInput", textInputValue);
        }

        @Test
        void shouldPassValidationForNOT_BLANKWhenThereIsAtLeast1CharacterInput() throws Exception {
            var nextPage = postExpectingSuccessAndFollowRedirect("notBlankPage", "notBlankInput", "something");
            assertThat(nextPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "123456",
                "1234",
                "1234e"
        })
        void shouldFailValidationForZipCodeWhenValueIsNotExactlyFiveDigits(String input) throws Exception {
            postAndAssertInputErrorDisplays("zipcodePage", "zipCodeInput", input);
        }

        @Test
        void shouldPassValidationForZipCodeWhenValueIsExactlyFiveDigits() throws Exception {
            var nextPage = postExpectingSuccessAndFollowRedirect("zipcodePage", "zipCodeInput", "12345");
            assertThat(nextPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "123",
                "12345678",
                "abcdefg",
                "1234-56",
                "1234e67"
        })
        void shouldFailValidationForCaseNumberWhenValueIsNotFourToSevenDigits(String input) throws Exception {
            postAndAssertInputErrorDisplays("caseNumberPage", "caseNumberInput", input);
        }

        @Test
        void shouldNotFailValidationForCaseNumberWhenValueIsEmptyWhenReturningToPage() throws Exception {
            postWithoutData("caseNumberPage").andExpect(redirectedUrl("/pages/caseNumberPage/navigation"));
            assertPageDoesNotHaveInputError("caseNumberPage", "caseNumberInput");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1234",
                "12345",
                "123456",
                "1234567"
        })
        void shouldPassValidationForCaseNumberWhenValueIsFourToSevenDigits(String input) throws Exception {
            var nextPage = postExpectingSuccessAndFollowRedirect("caseNumberPage", "caseNumberInput", input);
            assertThat(nextPage.getTitle()).isEqualTo(lastPageTitle);
        }

        @Test
        void shouldPassValidationForStateWhenValueIsAKnownStateCode_caseInsensitive() throws Exception {
            postAndAssertInputErrorDisplays("statePage", "stateInput", "XY");

            var nextPage = postExpectingSuccessAndFollowRedirect("statePage", "stateInput", "mn");
            assertThat(nextPage.getTitle()).isEqualTo(lastPageTitle);
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
        void shouldFailValidationForPhoneIfValueIsNotExactly10DigitsOrStartsWithAZeroOrOne(String input)
                throws Exception {
            postAndAssertInputErrorDisplays("phonePage", "phoneInput", input);
        }

        @Test
        void shouldPassValidationForPhoneIfAndOnlyIfValueIsExactly10Digits() throws Exception {
            var page = postExpectingSuccessAndFollowRedirect("phonePage", "phoneInput", "7234567890");
            assertThat(page.getTitle()).isEqualTo(lastPageTitle);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "a123",
                "1.",
                "51.787",
                "1.000",
                "-152"
        })
        void shouldFailValidationForMoneyWhenValueIsNotAWholeDollarAmount(String value) throws Exception {
            postAndAssertInputErrorDisplays("moneyPage", "moneyInput", value);
        }
    }
}
