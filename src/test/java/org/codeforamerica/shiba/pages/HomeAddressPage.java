package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class HomeAddressPage extends IntermediaryPage<WeDoNotRecommendMinimalFlowPage,MailingAddressPage> {
    public HomeAddressPage(WeDoNotRecommendMinimalFlowPage weDoNotRecommendMinimalFlowPage, RemoteWebDriver driver) {
        super(weDoNotRecommendMinimalFlowPage, driver);
    }

    @Override
    public MailingAddressPage getNextPage() {
        return new MailingAddressPage(this, driver);
    }

}
