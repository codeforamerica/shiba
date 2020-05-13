package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class TestFinalPage extends BasePage {
    private final HowItWorksPage howItWorksPage;
    @FindBy(partialLinkText = "Go Back")
    protected WebElement backButton;

    public TestFinalPage(RemoteWebDriver driver, HowItWorksPage howItWorksPage) {
        super(driver);
        this.howItWorksPage = howItWorksPage;
    }

    public HowItWorksPage goBack() {
        backButton.click();

        return howItWorksPage;
    }
}
