package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class SignThisApplicationPage extends IntermediaryPage<MailingAddressPage, SuccessPage> {
    public SignThisApplicationPage(MailingAddressPage mailingAddressPage, RemoteWebDriver driver) {
        super(mailingAddressPage, driver);
    }

    @Override
    public SuccessPage getNextPage() {
        return new SuccessPage(driver);
    }
}
