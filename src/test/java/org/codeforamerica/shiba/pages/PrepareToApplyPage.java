package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class PrepareToApplyPage extends BasePage {
    private RemoteWebDriver driver;

    @FindBy(partialLinkText = "Go Back")
    private WebElement backButton;

    public PrepareToApplyPage(RemoteWebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public LandingPage goBack() {
        backButton.click();

        return new LandingPage(driver);
    }

    public LanguagePreferencesPage clickPrimaryButton() {
        primaryButton.click();

        return new LanguagePreferencesPage(driver);
    }

}
