package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public class PersonalInfoPage extends IntermediaryPage<IntermediaryPage<HowItWorksPage, PersonalInfoPage>, TestFinalPage> {

    @FindBy(css = "input[name='firstName']")
    WebElement firstNameInput;

    @FindBy(css = "input[name='lastName']")
    WebElement lastNameInput;

    public PersonalInfoPage(IntermediaryPage<HowItWorksPage, PersonalInfoPage> previousPage, RemoteWebDriver driver) {
        super(previousPage, driver);
    }

    @Override
    public TestFinalPage getNextPage() {
        return new TestFinalPage(driver, this);
    }

    public void enterFirstName(String firstName) {
        firstNameInput.sendKeys(firstName);
    }

    public String getFirstNameValue() {
        return firstNameInput.getAttribute("value");
    }

    public void enterLastName(String lastName) {
        lastNameInput.sendKeys(lastName);
    }

    public String getLastNameValue() {
        return lastNameInput.getAttribute("value");
    }

    public boolean hasFirstNameError() {
        return !driver.findElements(By.cssSelector("input[name='firstName'] ~ p.text--error")).isEmpty();
    }

    public boolean hasLastNameError() {
        return !driver.findElements(By.cssSelector("input[name='lastName'] ~ p.text--error")).isEmpty();
    }
}
