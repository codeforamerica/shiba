package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class SignThisApplicationPage extends IntermediaryPage<LegalStuffPage, SuccessPage> {
    public SignThisApplicationPage(LegalStuffPage doYouNeedHelpImmediatelyPage, RemoteWebDriver driver) {
        super(doYouNeedHelpImmediatelyPage, driver);
    }

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
    }
}
