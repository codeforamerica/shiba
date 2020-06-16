package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WeDoNotRecommendMinimalFlowPage extends IntermediaryPage<ThanksPage, SuccessPage> {
    public WeDoNotRecommendMinimalFlowPage(ThanksPage thanksPage, RemoteWebDriver driver) {
        super(thanksPage, driver);
    }

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
    }

    public SuccessPage clickSubtleLink() {
        driver.findElement(By.className("link--subtle")).click();
        return new SuccessPage(driver);
    }

}
