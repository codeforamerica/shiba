package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class ContactInfoPage extends IntermediaryPage<PersonalInfoPage, ThanksPage>  {
    @FindBy(css = "input[name^='phoneNumber']")
    WebElement phoneNumberInput;

    public ContactInfoPage(PersonalInfoPage previousPage, RemoteWebDriver driver) {
        super(previousPage, driver);
    }

    @Override
    public ThanksPage getNextPage() {
        return new ThanksPage(this, driver);
    }

    public void enterPhoneNumber(String phone) {
        phoneNumberInput.clear();
        phoneNumberInput.sendKeys(phone);
    }

}
