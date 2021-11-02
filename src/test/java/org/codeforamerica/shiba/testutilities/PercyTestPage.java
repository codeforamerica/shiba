package org.codeforamerica.shiba.testutilities;

import io.percy.selenium.Percy;
import org.openqa.selenium.remote.RemoteWebDriver;

public class PercyTestPage extends Page {

  protected final Percy percy;

  public PercyTestPage(RemoteWebDriver driver) {
    super(driver);
    this.percy = new Percy(driver);
  }

  public void clickLink(String linkText) {
    percy.snapshot(driver.getTitle());
    super.clickLink(linkText);
  }

  public void clickButton(String buttonText) {
    percy.snapshot(driver.getTitle());
    super.clickButton(buttonText);
  }

  public void clickButtonLink(String buttonLinkText) {
    percy.snapshot(driver.getTitle());
    super.clickButtonLink(buttonLinkText);
  }

}
