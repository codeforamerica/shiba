package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LandingPage {

    @FindBy(css = "h1")
    private WebElement header;

    public LandingPage(RemoteWebDriver driver) {
        driver.navigate().to("http://localhost:8080/");
        PageFactory.initElements(driver, this);
    }

    public String getHeader() {
        return header.getText();
    }
}
