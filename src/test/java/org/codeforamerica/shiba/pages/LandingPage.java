package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class LandingPage extends BasePage {
    @FindBy(css = ".button--primary")
    protected WebElement primaryButton;

    public LandingPage(RemoteWebDriver driver) {
        super(driver);
    }

    public IntermediaryPage<LandingPage, LanguagePreferencesPage> clickPrimaryButton() {
        primaryButton.click();

        return new IntermediaryPage<>(this, driver) {
            @Override
            public LanguagePreferencesPage getNextPage() {
                return new LanguagePreferencesPage(driver, this);
            }
        };
    }
}
