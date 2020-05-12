package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class TestFinalPage extends BasePage {
    public TestFinalPage(RemoteWebDriver driver) {
        super(driver);
    }

    public ChooseProgramsPage goBack() {
        backButton.click();

        return new ChooseProgramsPage(driver);
    }
}
