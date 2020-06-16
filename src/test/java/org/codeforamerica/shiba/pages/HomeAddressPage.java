package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HomeAddressPage extends IntermediaryPage<WeDoNotRecommendMinimalFlowPage,SuccessPage> {
    public HomeAddressPage(WeDoNotRecommendMinimalFlowPage weDoNotRecommendMinimalFlowPage, RemoteWebDriver driver) {
        super(weDoNotRecommendMinimalFlowPage, driver);
    }

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
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
