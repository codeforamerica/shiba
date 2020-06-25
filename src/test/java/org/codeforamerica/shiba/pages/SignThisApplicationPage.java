package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class SignThisApplicationPage extends IntermediaryPage<DoYouNeedHelpImmediatelyPage, SuccessPage> {
    public SignThisApplicationPage(DoYouNeedHelpImmediatelyPage doYouNeedHelpImmediatelyPage, RemoteWebDriver driver) {
        super(doYouNeedHelpImmediatelyPage, driver);
    }

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
    }
}
