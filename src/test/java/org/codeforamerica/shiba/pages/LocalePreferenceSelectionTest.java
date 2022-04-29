package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.codeforamerica.shiba.testutilities.AbstractBasePageTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LocalePreferenceSelectionTest extends AbstractBasePageTest {

  @Override
  @BeforeEach
  protected void setUp() throws IOException {
    super.setUp();
    driver.navigate().to(baseUrl);
  }

  @AfterEach
  void tearDown() {
    testPage.selectFromDropdown("locales", "English");
  }

  @Test
  void userCanSeeTextInSpanishWhenSpanishIsLanguageSelected() {
    testPage.clickButton("Apply now");
    testPage.enter("county", "Hennepin");
    testPage.clickContinue();
    testPage.selectFromDropdown("locales", "Español");
    assertThat(driver.findElements(By.tagName("h1")).get(0).getText()).isEqualTo("Como funciona");

    testPage.clickButton("Continuar");
    testPage.clickButton("Continuar");

    WebElement selectedOption = testPage.getSelectedOption("writtenLanguage");
    assertThat(selectedOption.getText()).isEqualTo("Español");
  }

  @Test
  void userCanSeeSpanishWhenReadOrWriteSpanishIsSelectedOnLanguagePreferences() {
    testPage.clickButton("Apply now");
    testPage.enter("county", "Hennepin");
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.enter("writtenLanguage", "Español");
    assertThat(driver.getTitle()).isEqualTo("Preferencias de idioma");
    WebElement selectedOption = testPage.getSelectedOption("locales");
    assertThat(selectedOption.getText()).isEqualTo("Español");
  }
}
