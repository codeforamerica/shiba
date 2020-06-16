package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ThanksPage extends IntermediaryPage<ContactInfoPage, SuccessPage> {
    public ThanksPage(ContactInfoPage contactInfoPage, RemoteWebDriver driver) {
        super(contactInfoPage, driver);
    }

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
    }

    public WeDoNotRecommendMinimalFlowPage clickSubtleLink() {
        driver.findElement(By.className("link--subtle")).click();
        return new WeDoNotRecommendMinimalFlowPage(this, driver);
    }

}
