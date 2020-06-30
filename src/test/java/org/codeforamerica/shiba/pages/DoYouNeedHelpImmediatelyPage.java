package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DoYouNeedHelpImmediatelyPage extends BasePage {
    public DoYouNeedHelpImmediatelyPage(RemoteWebDriver driver) {
        super(driver);
    }

    public DoYouLiveAlonePage chooseYesIWantToSeeIfIQualify() {
        driver.findElement(By.linkText("Yes, I want to see if I qualify")).click();
        return new DoYouLiveAlonePage(driver);
    }

    public IntermediaryPage<DoYouNeedHelpImmediatelyPage, LegalStuffPage> chooseFinishApplicationNow() {
        driver.findElement(By.linkText("Finish application now")).click();
        return new IntermediaryPage<>(this, driver) {
            @Override
            protected LegalStuffPage getNextPage() {
                return new LegalStuffPage(this, driver);
            }
        };
    }
}
