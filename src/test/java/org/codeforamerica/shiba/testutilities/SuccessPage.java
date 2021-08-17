package org.codeforamerica.shiba.testutilities;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SuccessPage extends Page {

  @FindBy(id = "download-caf")
  private WebElement downloadCafLink;

  @FindBy(id = "download-ccap")
  private WebElement downloadCCAPApplicationLink;

  @FindBy(id = "application-id")
  private WebElement confirmationNumber;

  public SuccessPage(RemoteWebDriver driver) {
    super(driver);
    PageFactory.initElements(driver, this);
  }

  public void downloadPdfs() {
    if (CAFdownloadPresent()) {
      downloadCafLink.click();
    }
    if (CCAPdownloadPresent()) {
      downloadCCAPApplicationLink.click();
    }
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
    String[] confirmationNumParts =
        confirmationNumber.getText().split(" "); // ["Confirmation", "#:", "3830000507"]
    return confirmationNumParts[confirmationNumParts.length - 1];
  }

  public int pdfDownloadLinks() {
    return driver.findElementsByClassName("link--subtle").size();
  }
}
