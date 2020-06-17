package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class MailingAddressPage extends IntermediaryPage<HomeAddressPage, SuccessPage> {
    public MailingAddressPage(HomeAddressPage homeAddressPage, RemoteWebDriver driver) {
        super(homeAddressPage, driver);
    }

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
    }
}
