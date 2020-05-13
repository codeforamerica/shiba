package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class BasePage {
    protected final RemoteWebDriver driver;

    public String getTitle() {
        return driver.getTitle();
    }

    public BasePage(RemoteWebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
}
