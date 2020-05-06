package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LandingPage {

    @FindBy(css = "h1")
    private WebElement header;

    public LandingPage() {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.navigate().to("http://localhost:8080/");
        PageFactory.initElements(chromeDriver, this);
    }

    public String getHeader() {
        return header.getText();
    }
}
