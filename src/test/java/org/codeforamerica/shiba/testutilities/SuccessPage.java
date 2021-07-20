package org.codeforamerica.shiba.testutilities;

import org.codeforamerica.shiba.pages.Sentiment;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SuccessPage extends Page {
    @FindBy(linkText = "Combined Application")
    WebElement downloadCafLink;

    @FindBy(linkText = "Child Care Application")
    WebElement downloadCCAPApplicationLink;

    @FindBy(id = "application-id")
    WebElement confirmationNumber;

    public SuccessPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public void downloadPdfs() {
        if (CAFdownloadPresent()) downloadCafLink.click();
        if (CCAPdownloadPresent()) downloadCCAPApplicationLink.click();
    }

    public boolean CAFdownloadPresent() {
        try {
            return downloadCafLink.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean CCAPdownloadPresent() {
        try {
            return downloadCCAPApplicationLink.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getConfirmationNumber() {
        confirmationNumber.getText(); // "Confirmation #: 3830000507"
        String[] confirmationNumParts = confirmationNumber.getText().split(" "); // ["Confirmation", "#:", "3830000507"]
        return confirmationNumParts[confirmationNumParts.length - 1];
    }

    public void chooseSentiment(Sentiment sentiment) {
        driver.findElement(By.cssSelector(String.format("label[for='%s']", sentiment.name().toLowerCase()))).click();
    }

    public void submitFeedback() {
        driver.findElement(By.cssSelector("button")).click();
    }

    public int pdfDownloadLinks() {
        return driver.findElementsByClassName("button--link").size();
    }
}
