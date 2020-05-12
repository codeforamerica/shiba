package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public abstract class BasePage {
    @FindBy(partialLinkText = "Go Back")
    protected WebElement backButton;

    @FindBy(css = ".button--primary")
    protected WebElement primaryButton;

    protected final RemoteWebDriver driver;

    public String getTitle() {
        return driver.getTitle();
    }

    public BasePage(RemoteWebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    abstract BasePage goBack();
}
