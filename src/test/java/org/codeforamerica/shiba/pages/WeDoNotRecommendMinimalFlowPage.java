package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WeDoNotRecommendMinimalFlowPage extends IntermediaryPage<ThanksPage, HomeAddressPage> {
    public WeDoNotRecommendMinimalFlowPage(ThanksPage thanksPage, RemoteWebDriver driver) {
        super(thanksPage, driver);
    }

    @Override
    public HomeAddressPage getNextPage() {
        return new HomeAddressPage(this, driver);
    }

    public HomeAddressPage clickSubtleLink() {
        driver.findElement(By.className("link--subtle")).click();
        return new HomeAddressPage(this, driver);
    }

}
