package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class HomeAddressPage extends IntermediaryPage<WeDoNotRecommendMinimalFlowPage,SuccessPage> {
    public HomeAddressPage(WeDoNotRecommendMinimalFlowPage weDoNotRecommendMinimalFlowPage, RemoteWebDriver driver) {
        super(weDoNotRecommendMinimalFlowPage, driver);
    }

    @FindBy(css = "input[name^='sameMailingAddress']")
    List<WebElement> mailingAddressIsSameRadioButtons;

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
    }

    public void selectMailingAddressIsTheSame() {
        WebElement selectedMaritalStatus = mailingAddressIsSameRadioButtons.stream()
                .map(input -> input.findElement(By.xpath("./..")))
                .filter(label -> label.getText().contains("Yes"))
                .findFirst().orElseThrow();
        selectedMaritalStatus.click();
    }

    public void enterInput(String inputName, String input) {
        WebElement webElement = driver.findElement(By.cssSelector(String.format("input[name^='%s']", inputName)));
        webElement.clear();
        webElement.sendKeys(input);
    }
}
