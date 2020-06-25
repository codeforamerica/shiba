package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class MailingAddressPage extends IntermediaryPage<HomeAddressPage, DoYouNeedHelpImmediatelyPage> {
    public MailingAddressPage(HomeAddressPage homeAddressPage, RemoteWebDriver driver) {
        super(homeAddressPage, driver);
    }

    @Override
    public DoYouNeedHelpImmediatelyPage getNextPage() {
        return new DoYouNeedHelpImmediatelyPage(driver);
    }
}
