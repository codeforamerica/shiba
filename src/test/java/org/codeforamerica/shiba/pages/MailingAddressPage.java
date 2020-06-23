package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class MailingAddressPage extends IntermediaryPage<HomeAddressPage, SignThisApplicationPage> {
    public MailingAddressPage(HomeAddressPage homeAddressPage, RemoteWebDriver driver) {
        super(homeAddressPage, driver);
    }

    @Override
    public SignThisApplicationPage getNextPage() {
        return new SignThisApplicationPage(this, driver);
    }
}
