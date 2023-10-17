package org.codeforamerica.shiba.framework;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.List;
import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *  This test is dependent on the configurations set up in test-validation.yaml
 */

@Tag("validation")
@SpringBootTest(properties = {"pagesConfig=pages-config/test-validation.yaml"})
public class ValidationTest extends AbstractFrameworkTest {

  private final String errorMessage = "error message";
  private final String nextPageTitle = "next Page Title";
  private final String lastPageTitle = "last page title";
  private final String option1 = "option 1";
  private final String multipleValidationsPageTitle = "multiple validations page title";
  private final String moneyErrorMessageKey = "money is error";
  private final String emailErrorConKey = "Input con for com";
  @Override
  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    staticMessageSource.addMessage("first-page-title", ENGLISH, "first Page Title");
    staticMessageSource.addMessage("next-page-title", ENGLISH, nextPageTitle);
    staticMessageSource.addMessage("last-page-title", ENGLISH, lastPageTitle);
    staticMessageSource.addMessage("error-message-key", ENGLISH, errorMessage);
    staticMessageSource.addMessage("money-error-message-key", ENGLISH, moneyErrorMessageKey);
    staticMessageSource.addMessage("email-error-con-key",ENGLISH ,emailErrorConKey);
    staticMessageSource.addMessage("not-blank-error-message-key", ENGLISH, "not blank is error");
    staticMessageSource.addMessage("zip-code-page-title", ENGLISH, "zip code Page Title");
    staticMessageSource.addMessage("case-number-page-title", ENGLISH, "case number Page Title");
    staticMessageSource.addMessage("state-page-title", ENGLISH, "state page title");
    staticMessageSource.addMessage("phone-page-title", ENGLISH, "phone page title");
    staticMessageSource.addMessage("money-page-title", ENGLISH, "money page title");
    staticMessageSource.addMessage("number-title", ENGLISH, "hours per week page title");
    staticMessageSource.addMessage("ssn-page-title", ENGLISH, "ssn page title");
    staticMessageSource.addMessage("date-page-title", ENGLISH, "date page title");
    staticMessageSource.addMessage("dob-valid-page-title", ENGLISH, "dob valid page title");
    staticMessageSource.addMessage("email-page-title", ENGLISH, "email page title");
    staticMessageSource.addMessage("not-blank-page-title", ENGLISH, "not blank page title");
    staticMessageSource.addMessage("checkbox-page-title", ENGLISH, "checkbox page title");
    staticMessageSource.addMessage("option-1", ENGLISH, option1);
    staticMessageSource.addMessage("option-2", ENGLISH, "option-2");
    staticMessageSource.addMessage("page-with-input-with-multiple-validations",
        ENGLISH,
        multipleValidationsPageTitle);
  }

  @Test
  void shouldStayOnThePage_whenValidationFails() throws Exception {
    postWithoutData("firstPage").andExpect(redirectedUrl("/pages/firstPage"));
  }

  @Test
  void shouldGoOnToNextPage_whenValidationPasses() throws Exception {
    postExpectingNextPageTitle("firstPage", "someInputName", "something", nextPageTitle);
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
    postExpectingNextPageTitle("firstPage", "someInputName", "do not trigger validation",
        nextPageTitle);
  }

  @Test
  void shouldTriggerValidation_whenConditionIsTrue() throws Exception {
    assertPageDoesNotHaveInputError("firstPage", "someInputName");
    postExpectingFailureAndAssertErrorDisplaysOnDifferentInput("firstPage", "someInputName",
        "valueToTriggerCondition", "conditionalValidationWhenValueEquals");
  }

  @Test
  void shouldDisplayAllFailingValidationErrorMessages() throws Exception {
    postExpectingFailure("pageWithInputWithMultipleValidations", "multipleValidations", "");

    var page = new FormPage(getPage("pageWithInputWithMultipleValidations"));
    assertThat(page.getTitle()).isEqualTo(multipleValidationsPageTitle);
    var actualErrorMessages = page.getInputErrors("multipleValidations");
    assertThat(actualErrorMessages.text()).contains(moneyErrorMessageKey);
    assertThat(actualErrorMessages.text()).contains("not blank is error");
  }

  @Test
  void shouldStayOnPage_whenAnyValidationHasFailed() throws Exception {
    postExpectingFailure("pageWithInputWithMultipleValidations", "multipleValidations",
        "not money");

    var page = new FormPage(getPage("pageWithInputWithMultipleValidations"));
    assertThat(page.getTitle()).isEqualTo(multipleValidationsPageTitle);
    var actualErrorMessages = page.getInputErrors("multipleValidations");
    assertThat(actualErrorMessages.text()).contains(moneyErrorMessageKey);
    assertThat(actualErrorMessages.text()).doesNotContain("not blank is error");
  }


  @Nested
  @Tag("validation")
  class Condition {

    @Test
    void shouldTriggerValidation_whenConditionInputValueIsNoneSelected() throws Exception {
      postExpectingNextPageTitle("firstPage", "someInputName", "do not trigger validation",
          nextPageTitle);

      postWithoutData("nextPage").andExpect(redirectedUrl("/pages/nextPage"));
      assertPageHasInputError("nextPage", "conditionalValidationWhenValueIsNoneSelected");
    }

    @Test
    void shouldNotTriggerValidation_whenConditionInputValueIsSelected() throws Exception {
      postExpectingNextPageTitle("firstPage", "someInputName", "do not trigger validation",
          nextPageTitle);
      postExpectingNextPageTitle("nextPage", "someCheckbox", "VALUE_1", lastPageTitle);
    }

    @Test
    void shouldNotTriggerValidation_whenConditionInputContainsValue() throws Exception {
      postExpectingNextPageTitle("doesNotContainConditionPage", "triggerInput", "triggerValue",
          lastPageTitle);
    }

    @Test
    void shouldTriggerValidation_whenConditionInputDoesNotContainValue() throws Exception {
      postExpectingFailureAndAssertErrorDisplaysOnDifferentInput("doesNotContainConditionPage",
          "triggerInput", "not trigger", "conditionTest");
    }

    @Test
    void shouldTriggerValidation_whenConditionInputOnlyContainsValue() throws Exception {
      postExpectingNextPageTitle("containsStringOtherThanConditionPage",
          "triggerInput",
          "triggerValue",
          lastPageTitle);
    }

    @Test
    void shouldNotTriggerValidation_whenConditionInputContainsAnotherValue() throws Exception {
      postExpectingFailureAndAssertErrorDisplaysOnDifferentInput(
          "containsStringOtherThanConditionPage",
          "triggerInput",
          List.of("triggerValue", "something else"),
          "conditionTest");
    }

    @Test
    void shouldTriggerValidation_whenConditionInputIsEmptyOrBlank() throws Exception {
      postExpectingFailureAndAssertErrorDisplaysOnDifferentInput("emptyInputConditionPage",
          "triggerInput", "", "conditionTest");
    }

    @Test
    void shouldNotTriggerValidation_whenConditionInputIsNotEmptyOrBlank() throws Exception {
      postExpectingNextPageTitle("emptyInputConditionPage", "triggerInput", "something",
          lastPageTitle);
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
    void shouldFailValidationForNOT_BLANKWhenThereIsEmptyOrBlankInput(String textInputValue)
        throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("notBlankPage", "notBlankInput",
          textInputValue);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "value1",
        "value2   ","  value3"
    })
    void shouldFailValidationForSHOULD_BE_BLANKWhenThereIsnotEmptyOrBlankInput(String textInputValue)
        throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("shouldbeBlankPage", "shouldbeBlankInput",
          textInputValue);
    }

    @Test
    void shouldPassValidationForNOT_BLANKWhenThereIsAtLeast1CharacterInput() throws Exception {
      postExpectingNextPageTitle("notBlankPage",
          "notBlankInput",
          "something",
          lastPageTitle);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123456",
        "1234",
        "1234e"
    })
    void shouldFailValidationForZipCodeWhenValueIsNotExactlyFiveDigits(String input)
        throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("zipcodePage", "zipCodeInput", input);
    }

    @Test
    void shouldPassValidationForZipCodeWhenValueIsExactlyFiveDigits() throws Exception {
      postExpectingNextPageTitle("zipcodePage", "zipCodeInput", "12345", lastPageTitle);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123",
        "12345678",
        "abcdefg",
        "1234-56",
        "1234e67"
    })
    void shouldFailValidationForCaseNumberWhenValueIsNotFourToSevenDigits(String input)
        throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("caseNumberPage", "caseNumberInput",
          input);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "123",
        "123456789",
        "abcdefg",
        "1234-56",
        "1234e67"
    })
    void shouldFailValidationForHealthCareUploadCaseNumberWhenValueIsNotFourToEightDigits(String input)
        throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("healthCareCaseNumberPage", "caseNumberInput", input);
    }

    @Test
    void shouldNotFailValidationForCaseNumberWhenValueIsEmptyWhenReturningToPage()
        throws Exception {
      postWithoutData("caseNumberPage")
          .andExpect(redirectedUrl("/pages/caseNumberPage/navigation"));
      assertPageDoesNotHaveInputError("caseNumberPage", "caseNumberInput");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1234",
        "12345",
        "123456",
        "1234567"
    })
    void shouldPassValidationForCaseNumberWhenValueIsFourToSevenDigits(String input)
        throws Exception {
      postExpectingNextPageTitle("caseNumberPage", "caseNumberInput", input, lastPageTitle);
    }

    @Test
    void shouldPassValidationForStateWhenValueIsAKnownStateCode_caseInsensitive() throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("statePage", "stateInput", "XY");

      postExpectingNextPageTitle("statePage", "stateInput", "mn", lastPageTitle);
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
      postExpectingFailureAndAssertErrorDisplaysForThatInput("phonePage", "phoneInput", input);
    }

    @Test
    void shouldPassValidationForPhoneIfAndOnlyIfValueIsExactly10Digits() throws Exception {
      postExpectingNextPageTitle("phonePage", "phoneInput", "7234567890", lastPageTitle);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "a123",
        "1.",
        "51.787",
        "1.000",
        "-152"
    })
    void shouldFailValidationForMoneyWhenValueIsNotAWholeDollarAmount(String value)
        throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("moneyPage", "moneyInput", value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "14",
        "1.1",
        "16.71",
        "11,000",
        "11,000.15"
    })
    void shouldPassValidationForMoneyWhenValueIsAPositiveWholeDollarAmount(String value)
        throws Exception {
      postExpectingNextPageTitle("moneyPage", "moneyInput", value, lastPageTitle);
    }


    @Test
    void shouldPassValidationForValidNumber() throws Exception {
      postExpectingNextPageTitle("numberPage", "numberInput", "30", lastPageTitle);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "a123",
        "1.",
        "20-30",
        "-152"
    })
    void shouldFailValidationForInvalidNumber(String value) throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("numberPage", "numberInput", value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1234567890",
        "12345678",
        "12345678e"
    })
    void shouldFailValidationForSSNWhenValueIsNotExactlyNineDigits(String input) throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("ssnPage", "ssnInput", input);
    }

    @Test
    void shouldPassValidationForSSNWhenValueIsExactlyNineDigits() throws Exception {
      postExpectingNextPageTitle("ssnPage", "ssnInput", "123456789", lastPageTitle);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "13,42,1492",
        "0,2,1929",
        "1,2,929",
    })
    void shouldFailValidationForDateWhenValueIsNotAValidDate(String month, String day,
        String year) throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatDateInput("datePage", "dateInput",
          List.of(month, day, year));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "11,12,149l)",
        "0,2,19a9",
        "1,2,abcd",
    })
    void shouldFailValidationForDateWhenLetterIsInYear(String month, String day,
        String year) throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatDateInput("datePage", "dateInput",
          List.of(month, day, year));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "02,20,1492",
        "1,2,1929",
    })
    void shouldPassValidationForDateWhenValueIsAValidDate(String month, String day, String year)
        throws Exception {
      postExpectingNextPageTitle("datePage", "dateInput", List.of(month, day, year), lastPageTitle);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "12,31,1899",
        "1,1,3000"
    })
    void shouldFailValidationForDobValidWhenValueIsAnInvalidDate(String month, String day,
        String year) throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatDateInput("dobValidPage", "dobValidInput",
          List.of(month, day, year));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "12,31,195p",
        "1,1,200q"
    })
    void shouldFailValidationForDobValidWhenValueContainsLetter(String month, String day,
        String year) throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatDateInput("dobValidPage", "dobValidInput",
          List.of(month, day, year));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "01,02,1900",
        "9,9,2020",
    })
    void shouldPassValidationForDobValidWhenValueIsAValidDate(String month, String day, String year)
        throws Exception {
      postExpectingNextPageTitle("dobValidPage", "dobValidInput", List.of(month, day, year),
          lastPageTitle);
    }

    @Test
    void shouldFailValidationForSELECT_AT_LEAST_ONEWhenNoValuesAreSelected() throws Exception {
      postWithoutData("checkboxPage").andExpect(redirectedUrl("/pages/checkboxPage"));
    }

    @Test
    void shouldPassValidationForSELECT_AT_LEAST_ONEWhenAtLeastOneValueIsSelected()
        throws Exception {
      postExpectingNextPageTitle("checkboxPage", "checkboxInput", option1, lastPageTitle);
    }

    @ParameterizedTest
    @ValueSource(strings = {"asdf", "almost@.com", "\" \" , "})
    void shouldFailValidationForEMAILWhenThereIsAnInvalidEmail(String email) throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("pageWithEmail", "emailInput", email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake@test.com", "FAKE@TEST.COM"})
    void shouldPassValidationForEMAILWhenThereIsAValidEmail(String email) throws Exception {
      postExpectingNextPageTitle("pageWithEmail", "emailInput", email, lastPageTitle);
    }

    @Test
    void shouldPassValidationForEMAILWhenValidEmailHasTrailingWhitespace() throws Exception {
      postExpectingNextPageTitle("pageWithEmail", "emailInput", "fake@test.com ", lastPageTitle);
    }

    @Test
    void shouldFailValidationForEMAIL_DOES_NOT_END_WITH_CONWhenEmailHasDotConTypo() throws Exception {
      postExpectingFailureAndAssertErrorDisplaysForThatInput("pageWithEmail", "emailInput", "test@email.con", emailErrorConKey);
    }
    
    @Test
    void shouldPassValidationForCounty() throws Exception {
      postExpectingNextPageTitle("identifyCountyBeforeApplying", "county", "Aitkin", lastPageTitle);
    }
    
    @Test
    void shouldFailValidationForInvalidCountyInput() throws Exception {
    	postExpectingFailure("identifyCountyBeforeApplying", "county", "maliciousStringGiveMeYourPasswords");
    }
    
    @Test
    void shouldPassValidationForTribalNation() throws Exception {
      postExpectingNextPageTitle("identifyCountyOrTribalNation", "tribalNation", "Mille Lacs Band of Ojibwe", lastPageTitle);
    }
    
    @Test
    void shouldFailValidationForInvalidTribalNationInput() throws Exception {
    	postExpectingFailure("identifyCountyOrTribalNation", "tribalNation", "maliciousStringGiveMeYourPasswords");
    }
    
  }
}
