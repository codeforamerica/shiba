package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DoYouNeedHelpImmediatelyPage extends BasePage {
    public DoYouNeedHelpImmediatelyPage(RemoteWebDriver driver) {
        super(driver);
    }

    public SignThisApplicationPage clickFinishApplicationNow() {
        driver.findElement(By.cssSelector("button")).click();
        return new SignThisApplicationPage(this, driver);
    }

    public DoYouLiveAlonePage clickFirstOption() {
        driver.findElement(By.cssSelector(".button:first-of-type")).click();
        return new DoYouLiveAlonePage(driver);
    }
}
