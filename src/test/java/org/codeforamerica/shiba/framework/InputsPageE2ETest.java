package org.codeforamerica.shiba.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.testutilities.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
    "pagesConfig=pages-config/test-input.yaml"})
@Tag("framework")
public class InputsPageE2ETest extends AbstractExistingStartTimePageTest {

  final String radioOption1 = "radio option 1";
  final String radioOption2 = "option-2";
  final String checkboxOption1 = "checkbox option 1";
  final String checkboxOption2 = "checkbox option 2";
  final String noneCheckboxOption = "none checkbox option";
  final String selectOption1 = "select option 1";
  final String selectOption2 = "select option 2";
  final String hideFollowUpValue = "YEP";
  final String showFollowUpValue = "NOPE";
  final String otherShowFollowUpValue = "UNSURE";
  final String promptMessage = "prompt message";
  final String helpMessage = "help message";
  final String optionHelpMessage = "option help message";
  final String placeholder = "optional input";

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
    staticMessageSource.addMessage("follow-up-true", Locale.ENGLISH, hideFollowUpValue);
    staticMessageSource.addMessage("follow-up-false", Locale.ENGLISH, showFollowUpValue);
    staticMessageSource.addMessage("follow-up-uncertain", Locale.ENGLISH, otherShowFollowUpValue);
    staticMessageSource.addMessage("prompt-message-key", Locale.ENGLISH, promptMessage);
    staticMessageSource.addMessage("help-message-key", Locale.ENGLISH, helpMessage);
    staticMessageSource.addMessage("option-help-key", Locale.ENGLISH, optionHelpMessage);
    staticMessageSource.addMessage("general.optional", Locale.ENGLISH, placeholder);
    staticMessageSource.addMessage("general.month", Locale.ENGLISH, "month");
    staticMessageSource.addMessage("general.day", Locale.ENGLISH, "day");
    staticMessageSource.addMessage("general.year", Locale.ENGLISH, "year");
  }

  @Test
  void uneditableInputsShouldBeUneditable() {
    driver.navigate().to(baseUrl + "/pages/firstPage");
    WebElement uneditableInput = driver
        .findElement(By.cssSelector("input[name='uneditableInput[]']"));
    uneditableInput.sendKeys("new value");
    assertThat(uneditableInput.getAttribute("value")).isEqualTo("default value");
  }

  @Test
  void specialInputsShouldBehaveCorrectly() {
    // Uneditable inputs should be be the same when returning to a page
    driver.navigate().to(baseUrl + "/pages/firstPage");
    driver.findElement(By.tagName("button")).click();
    assertThat(driver.getTitle()).isEqualTo("nextPageTitle");
    driver.findElement(By.partialLinkText("Go Back")).click();
    assertThat(driver.getTitle()).isEqualTo("firstPageTitle");
    assertThat(
        driver.findElement(By.cssSelector(String.format("input[name='%s[]']", "uneditableInput")))
            .getAttribute("value")).contains("default value");

    // Checking the "None of the above" checkbox should uncheck any other checkboxes
    testPage.enter("checkboxInput", List.of(checkboxOption1, checkboxOption2));
    testPage.enter("checkboxInput", noneCheckboxOption);
    assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(noneCheckboxOption);

    // Checking any other checkboxes should uncheck the "None of the above" checkbox
    testPage.enter("checkboxInput", checkboxOption1);
    assertThat(testPage.getCheckboxValues("checkboxInput")).containsOnly(checkboxOption1);

    // Follow ups should be hidden when then input value does not match the followUpValues
    var radioInput = "radioInputWithFollowUps";
    testPage.enter(radioInput, hideFollowUpValue);
    assertThat(followUpIsDisplayedForInput(radioInput)).isFalse();
    var checkboxInput = "checkboxInputWithFollowUps";
    testPage.enter(checkboxInput, hideFollowUpValue);
    assertThat(followUpIsDisplayedForInput(checkboxInput)).isFalse();

    // Follow ups should be shown when the input value matches one of the followUpValues
    testPage.enter(radioInput, showFollowUpValue);
    assertThat(followUpIsDisplayedForInput(radioInput)).isTrue();
    testPage.enter(checkboxInput, showFollowUpValue);
    assertThat(followUpIsDisplayedForInput(checkboxInput)).isTrue();
    testPage.enter(radioInput, otherShowFollowUpValue);
    assertThat(followUpIsDisplayedForInput(radioInput)).isTrue();
    testPage.enter(checkboxInput, otherShowFollowUpValue);
    assertThat(followUpIsDisplayedForInput(checkboxInput)).isTrue();

    // Follow ups should show on a checkbox when one of the followUpValues is unchecked, as long as another followUpValue is still checked
    testPage.enter(checkboxInput, List.of(showFollowUpValue, otherShowFollowUpValue));
    testPage.enter(checkboxInput, otherShowFollowUpValue);
    assertThat(followUpIsDisplayedForInput(checkboxInput)).isTrue();

    // Follow up should show when returning to the page, and their values should be preserved
    String followUpTextInputValue = "some follow up";
    testPage.enter(followUpInputName(radioInput), followUpTextInputValue + " for radio");
    testPage.enter(followUpInputName(checkboxInput), followUpTextInputValue + " for checkbox");
    testPage.clickContinue();
    testPage.goBack();
    assertThat(followUpIsDisplayedForInput(radioInput)).isTrue();
    assertThat(followUpIsDisplayedForInput(checkboxInput)).isTrue();
    assertThat(testPage.getInputValue(followUpInputName(radioInput)))
        .isEqualTo(followUpTextInputValue + " for radio");
    assertThat(testPage.getInputValue(followUpInputName(checkboxInput)))
        .isEqualTo(followUpTextInputValue + " for checkbox");
  }

  private String followUpInputName(String radioInput) {
    return radioInput + "-followUpTextInput";
  }

  private boolean followUpIsDisplayedForInput(String radioInputWithFollowUps) {
    var selector = "input[name='%s-followUpTextInput[]']".formatted(radioInputWithFollowUps);
    return driver.findElement(By.cssSelector(selector)).isDisplayed();
  }
}
