package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class LandingPage extends BasePageObject {
    public LandingPage(RemoteWebDriver driver) {
        super(driver);
    }

    public PrepareToApplyPage clickPrimaryButton() {
        primaryButton.click();

        return new PrepareToApplyPage(driver);
    }
}
