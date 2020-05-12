package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class LandingPage extends BasePage {
    public LandingPage(RemoteWebDriver driver) {
        super(driver);
    }

    @Override
    BasePage goBack() {
        throw new RuntimeException("Landing page do not have go back button");
    }

    public PrepareToApplyPage clickPrimaryButton() {
        primaryButton.click();

        return new PrepareToApplyPage(driver);
    }
}
