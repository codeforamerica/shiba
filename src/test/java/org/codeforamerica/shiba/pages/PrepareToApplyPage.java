package org.codeforamerica.shiba.pages;

import org.openqa.selenium.remote.RemoteWebDriver;

public class PrepareToApplyPage extends IntermediaryPage<LandingPage, LanguagePreferencesPage> {
    private final RemoteWebDriver driver;

    public PrepareToApplyPage(RemoteWebDriver driver, LandingPage landingPage) {
        super(landingPage, driver);
        this.driver = driver;
    }

    @Override
    public LanguagePreferencesPage getNextPage() {
        return new LanguagePreferencesPage(driver, this);
    }

}
