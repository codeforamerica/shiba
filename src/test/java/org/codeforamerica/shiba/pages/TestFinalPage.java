package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class TestFinalPage extends BasePage {
    private final PersonalInfoPage previousPage;

    @FindBy(partialLinkText = "Go Back")
    protected WebElement backButton;

    public TestFinalPage(RemoteWebDriver driver, PersonalInfoPage previousPage) {
        super(driver);
        this.previousPage = previousPage;
    }

    @SuppressWarnings("unused")
    public PersonalInfoPage goBack() {
        backButton.click();

        return previousPage;
    }
}
