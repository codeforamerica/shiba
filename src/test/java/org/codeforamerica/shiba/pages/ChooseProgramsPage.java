package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class ChooseProgramsPage extends BasePage {
    @FindBy(partialLinkText = "Go Back")
    private WebElement backButton;

    public ChooseProgramsPage(RemoteWebDriver driver) {
        super(driver);
    }

    public LanguagePreferencesPage goBack() {
        backButton.click();

        return new LanguagePreferencesPage(driver);
    }
}
