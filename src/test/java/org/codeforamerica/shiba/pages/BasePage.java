package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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

    public void enterInput(String inputName, String input) {
        WebElement webElement = driver.findElement(By.cssSelector(String.format("input[name^='%s']", inputName)));
        webElement.clear();
        webElement.sendKeys(input);
    }

}
