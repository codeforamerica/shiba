package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class PersonalInfoPage extends IntermediaryPage<IntermediaryPage<HowItWorksPage, PersonalInfoPage>, ContactInfoPage> {

    @FindBy(css = "input[name^='first-name']")
    WebElement firstNameInput;

    @FindBy(css = "input[name^='last-name']")
    WebElement lastNameInput;

    @FindBy(css = "input[name^='other-name'")
    WebElement otherNameInput;

    @FindBy(css = "input[name^='date-of-birth']")
    List<WebElement> dateOfBirthInputs;

    @FindBy(css = "input[name^='ssn']")
    WebElement ssnInput;

    @FindBy(css = "input[name^='marital-status']")
    List<WebElement> maritalStatusInputs;

    @FindBy(css = "input[name^='sex']")
    List<WebElement> sexInputs;

    @FindBy(css = "input[name^='lived-in-mn-whole-life']")
    List<WebElement> livedInMNWholeLife;

    public PersonalInfoPage(IntermediaryPage<HowItWorksPage, PersonalInfoPage> previousPage, RemoteWebDriver driver) {
        super(previousPage, driver);
    }

    @Override
    public ContactInfoPage getNextPage() {
        return new ContactInfoPage(this, driver);
    }

    public void enterFirstName(String firstName) {
        firstNameInput.clear();
        firstNameInput.sendKeys(firstName);
    }

    public String getFirstNameValue() {
        return firstNameInput.getAttribute("value");
    }

    public void enterLastName(String lastName) {
        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);
    }

    public String getLastNameValue() {
        return lastNameInput.getAttribute("value");
    }

    public boolean hasFirstNameError() {
        return !driver.findElements(By.cssSelector("input[name^='first-name'] ~ p.text--error")).isEmpty();
    }

    public boolean hasLastNameError() {
        return !driver.findElements(By.cssSelector("input[name^='last-name'] ~ p.text--error")).isEmpty();
    }

    public void enterOtherName(String otherName) {
        otherNameInput.clear();
        otherNameInput.sendKeys(otherName);
    }

    public String getOtherNameValue() {
        return otherNameInput.getAttribute("value");
    }

    public void enterBirthDate(DatePart datePart, String value) {
        WebElement input = dateOfBirthInputs.get(datePart.getPosition() - 1);
        input.clear();
        input.sendKeys(value);
    }

    public String getBirthDateValue(DatePart datePart) {
        return dateOfBirthInputs.get(datePart.getPosition() - 1).getAttribute("value");
    }

    public void enterSSN(String ssn) {
        ssnInput.clear();
        ssnInput.sendKeys(ssn);
    }

    public String getSsnValue() {
        return ssnInput.getAttribute("value");
    }

    public boolean hasSSNError() {
        return !driver.findElements(By.cssSelector("input[name^='ssn'] ~ p.text--error")).isEmpty();
    }

    public void selectMaritalStatus(String maritalStatus) {
        WebElement selectedMaritalStatus = maritalStatusInputs.stream()
                .map(input -> input.findElement(By.xpath("./..")))
                .filter(label -> label.getText().equals(maritalStatus))
                .findFirst().orElseThrow();
        selectedMaritalStatus.click();
    }

    public String getMaritalStatus() {
        return maritalStatusInputs.stream()
                .filter(WebElement::isSelected)
                .map(input -> input.findElement(By.xpath("./..")).getText())
                .findFirst().orElse(null);
    }

    public void selectSex(String sex) {
        WebElement selectedMaritalStatus = sexInputs.stream()
                .map(input -> input.findElement(By.xpath("./..")))
                .filter(label -> label.getText().equals(sex))
                .findFirst().orElseThrow();
        selectedMaritalStatus.click();
    }

    public String getSex() {
        return sexInputs.stream()
                .filter(WebElement::isSelected)
                .map(input -> input.findElement(By.xpath("./..")).getText())
                .findFirst().orElse(null);
    }

    public void selectLivedInMNWholeLife(String hasLivedInMNWholeLife) {
        WebElement selectedMaritalStatus = livedInMNWholeLife.stream()
                .map(input -> input.findElement(By.xpath("./..")))
                .filter(label -> label.getText().equals(hasLivedInMNWholeLife))
                .findFirst().orElseThrow();
        selectedMaritalStatus.click();
    }

    public String getLivedInMNWholeLife() {
        return livedInMNWholeLife.stream()
                .filter(WebElement::isSelected)
                .map(input -> input.findElement(By.xpath("./..")).getText())
                .findFirst().orElse(null);
    }

    public void enterMoveToMNDatePart(DatePart datePart, String month) {
        WebElement input = driver.findElement(By.cssSelector(String.format("input[name^='move-to-mn-date']:nth-of-type(%s)", datePart.getPosition())));
        input.clear();
        input.sendKeys(month);
    }

    public boolean displaysAllMoveToMNInputs() {
        return driver.findElements(By.cssSelector("input[name^='move-to-mn']")).stream().allMatch(WebElement::isDisplayed);
    }

    public boolean displaysNoMoveToMNInputs() {
        return driver.findElements(By.cssSelector("input[name^='move-to-mn']")).stream().noneMatch(WebElement::isDisplayed);
    }

    public String getMoveToMNDate(DatePart datePart) {
        return driver.findElements(By.cssSelector(String.format("input[name^='move-to-mn-date']:nth-of-type(%s)", datePart.getPosition()))).stream().findFirst()
        .map(input -> input.getAttribute("value"))
        .orElse(null);
    }

    public void enterPreviousCity(String city) {
        WebElement input = driver.findElement(By.cssSelector("input[name^='move-to-mn-previous-city']"));
        input.clear();
        input.sendKeys(city);
    }

    public String getPreviousCity() {
        return driver.findElements(By.cssSelector("input[name^='move-to-mn-previous-city']")).stream().findFirst()
                .map(input -> input.getAttribute("value"))
                .orElse(null);
    }
}
