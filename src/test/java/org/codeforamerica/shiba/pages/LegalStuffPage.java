package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class LegalStuffPage extends IntermediaryPage<DoYouNeedHelpImmediatelyPage, SignThisApplicationPage> {
    public LegalStuffPage(DoYouNeedHelpImmediatelyPage doYouNeedHelpImmediatelyPage, RemoteWebDriver driver) {
        super(doYouNeedHelpImmediatelyPage, driver);
    }

    @FindBy(css = "input[name^='agreeToTerms']")
    WebElement agreeToTermsCheckBox;

    @Override
    public SignThisApplicationPage getNextPage() {
        return new SignThisApplicationPage(this, driver);
    }

    public void clickIAgree() {
        agreeToTermsCheckBox.click();
    }
}
