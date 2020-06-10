package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ContactInfoPage extends IntermediaryPage<PersonalInfoPage, ThanksPage>  {
    @FindBy(css = "input[name^='phoneNumber']")
    WebElement phoneNumberInput;

    @FindBy(css = "input[name^='email']")
    WebElement emailInput;

    @FindBy(css = ".checkbox")
    List<WebElement> phoneOrEmail;

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

    public void selectContactChoice(String choice) {
        WebElement optionToChoose = phoneOrEmail.stream()
                .filter(option -> option.findElement(By.tagName("span")).getText().equals(choice))
                .findFirst().orElseThrow();

        optionToChoose.click();
    }

    public boolean hasEmailError() {
        return !driver.findElements(By.cssSelector("input[name^='email'] ~ p.text--error")).isEmpty();
    }

    public void enterEmail(String email) {
        emailInput.clear();
        emailInput.sendKeys(email);
    }
}
