package org.codeforamerica.shiba.testutilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SuccessPage extends Page {

  @FindBy(id = "download")
  private WebElement downloadLink;


  public SuccessPage(RemoteWebDriver driver) {
    super(driver);
    PageFactory.initElements(driver, this);
  }

  public void downloadPdfZipFile() {
      downloadLink.click();
  }

  public int countDownloadLinks() {
    return driver.findElements(By.className("link--subtle")).size();
  }
}
