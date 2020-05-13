package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HowItWorksPage extends BasePage {
    public HowItWorksPage(RemoteWebDriver driver) {
        super(driver);
    }

    @Override
    public BasePage goBack() {
        backButton.click();

        return new ChooseProgramsPage(driver);
    }

    public boolean headerIncludesProgram(String program) {
        return driver.findElement(By.tagName("h2")).getText().contains(program);
    }
}
