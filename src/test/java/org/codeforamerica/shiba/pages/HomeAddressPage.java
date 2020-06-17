package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HomeAddressPage extends IntermediaryPage<WeDoNotRecommendMinimalFlowPage,MailingAddressPage> {
    public HomeAddressPage(WeDoNotRecommendMinimalFlowPage weDoNotRecommendMinimalFlowPage, RemoteWebDriver driver) {
        super(weDoNotRecommendMinimalFlowPage, driver);
    }

    @Override
    public MailingAddressPage getNextPage() {
        return new MailingAddressPage(this, driver);
    }

    public void checkImHomeless() {
        driver.findElement(By.cssSelector("label.checkbox")).click();
    }

    public void enterInput(String inputName, String input) {
        WebElement webElement = driver.findElement(By.cssSelector(String.format("input[name^='%s']", inputName)));
        webElement.clear();
        webElement.sendKeys(input);
    }
}
