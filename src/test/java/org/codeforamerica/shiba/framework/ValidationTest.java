package org.codeforamerica.shiba.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        postWithData("firstPage", "someInputName", "something").andExpect(status().is3xxRedirection());
        var page = getNextPage("firstPage");
        assertThat(page.getTitle()).isEqualTo(nextPageTitle);
    }

    @Test
    void shouldClearValidationError_afterErrorHasBeenFixed() throws Exception {
        // Submit the page without required fields filled out
        getPage("firstPage").andExpect(pageDoesNotHaveInputError());
        postWithoutData("firstPage").andExpect(redirectedUrl("/pages/firstPage"));

        // Submit with required fields filled out this time
        getPage("firstPage").andExpect(pageHasInputError());
        postWithData("firstPage", "someInputName", "not blank");

        // When I hit the back button, no input error should be displayed
        getPage("firstPage").andExpect(pageDoesNotHaveInputError());
    }

    @Test
    void shouldDisplayErrorMessageWhenValidationFailed() throws Exception {
        getPage("firstPage").andExpect(pageDoesNotHaveInputError());
        postWithoutData("firstPage").andExpect(redirectedUrl("/pages/firstPage"));

        var page = new FormPage(getPage("firstPage"));
        assertThat(page.getInputError("someInputName").text()).isEqualTo(errorMessage);
    }

    @Test
    void shouldNotTriggerValidation_whenConditionIsFalse() throws Exception {
        getPage("firstPage").andExpect(pageDoesNotHaveInputError());
        postWithData("firstPage", "someInputName", "do not trigger validation");
        var page = getNextPage("firstPage");
        assertThat(page.getTitle()).isEqualTo(nextPageTitle);
    }

    @Test
    void shouldTriggerValidation_whenConditionIsTrue() throws Exception {
        getPage("firstPage").andExpect(pageDoesNotHaveInputError());
        postWithData("/pages/firstPage",
                     "/pages/firstPage",
                     Map.of("someInputName", List.of("valueToTriggerCondition")));

        var page = new FormPage(getPage("firstPage").andReturn());
        assertTrue(page.hasInputError("conditionalValidationWhenValueEquals"));
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
    class SpecificValidations {
        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   "
        })
        void shouldFailValidationForNOT_BLANKWhenThereIsEmptyOrBlankInput(String textInputValue) throws Exception {
            postExpectingFailure("notBlankPage", "notBlankInput", textInputValue);
            var page = new FormPage(getPage("notBlankPage"));
            assertTrue(page.hasInputError("notBlankInput"));
        }

        @Test
        void shouldPassValidationForNOT_BLANKWhenThereIsAtLeast1CharacterInput() throws Exception {
            postWithData("notBlankPage", "notBlankInput", "something");
            var page = getNextPage("notBlankPage");
            assertThat(page.getTitle()).isEqualTo(lastPageTitle);
        }
    }
}
