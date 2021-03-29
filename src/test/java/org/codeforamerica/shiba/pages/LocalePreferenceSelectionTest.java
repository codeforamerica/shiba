package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractBasePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalePreferenceSelectionTest extends AbstractBasePageTest {

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl);
    }

    @Test
    void userCanSeeTextInSpanishWhenSpanishIsLanguageSelected() {
        testPage.clickButton("Apply now");
        testPage.enter("locales", "Español");
        assertThat(driver.findElements(By.tagName("h1")).get(0).getText()).isEqualTo("Como funciona");

        testPage.clickButton("Continuar");

        String elementId = "writtenLanguage";
        WebElement selectedOption = testPage.getSelectedOption(elementId);
        assertThat(selectedOption.getText()).isEqualTo("Español");
    }

    @Test
    void userCanSeeSpanishWhenReadOrWriteSpanishIsSelectedOnLanguagePreferences() {
        testPage.clickButton("Apply now");
        testPage.clickContinue();
        testPage.enter("writtenLanguage", "Español");
        assertThat(driver.getTitle()).isEqualTo("Preferencias de idioma");
        String elementId = "locales";
        WebElement selectedOption = testPage.getSelectedOption(elementId);
        assertThat(selectedOption.getText()).isEqualTo("Español");
    }
}
