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

    public PrepareToApplyPage clickPrimaryButton() {
        primaryButton.click();

        return new PrepareToApplyPage(driver, this);
    }
}
