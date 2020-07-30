package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SuccessPage extends Page {
    @FindBy(linkText = "Download My Receipt")
    WebElement downloadReceiptButton;

    @FindBy(linkText = "Download XML")
    WebElement downloadXMLButton;

    public SuccessPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void downloadReceipt() {
        downloadReceiptButton.click();
    }

    public void downloadXML() {
        downloadXMLButton.click();
    }

    public String getSubmissionTime() {
        return driver.findElement(By.id("submission-date")).getText();
    }
}
