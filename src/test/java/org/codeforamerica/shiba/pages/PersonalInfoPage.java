package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class PersonalInfoPage extends IntermediaryPage<IntermediaryPage<HowItWorksPage, PersonalInfoPage>, TestFinalPage> {

    @FindBy(css = "input[name='firstName']")
    WebElement firstNameInput;

    @FindBy(css = "input[name='lastName']")
    WebElement lastNameInput;

    @FindBy(css = "input[name='otherName'")
    WebElement otherNameInput;

    @FindBy(css = "input[name='birthMonth'")
    WebElement birthMonthInput;

    @FindBy(css = "input[name='birthDay'")
    WebElement birthDayInput;

    @FindBy(css = "input[name='birthYear'")
    WebElement birthYearInput;

    @FindBy(css = "input[name='ssn']")
    WebElement ssnInput;

    @FindBy(css = "input[name='maritalStatus']")
    List<WebElement> maritalStatusInputs;

    @FindBy(css = "input[name='sex']")
    List<WebElement> sexInputs;

    @FindBy(css = "input[name='livedInMNWholeLife']")
    List<WebElement> livedInMNWholeLife;

    public PersonalInfoPage(IntermediaryPage<HowItWorksPage, PersonalInfoPage> previousPage, RemoteWebDriver driver) {
        super(previousPage, driver);
    }

    @Override
    public TestFinalPage getNextPage() {
        return new TestFinalPage(driver, this);
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
        return !driver.findElements(By.cssSelector("input[name='firstName'] ~ p.text--error")).isEmpty();
    }

    public boolean hasLastNameError() {
        return !driver.findElements(By.cssSelector("input[name='lastName'] ~ p.text--error")).isEmpty();
    }

    public void enterOtherName(String otherName) {
        otherNameInput.clear();
        otherNameInput.sendKeys(otherName);
    }

    public String getOtherNameValue() {
        return otherNameInput.getAttribute("value");
    }

    public void enterBirthMonth(String month) {
        birthMonthInput.clear();
        birthMonthInput.sendKeys(month);
    }

    public void enterBirthDay(String day) {
        birthDayInput.clear();
        birthDayInput.sendKeys(day);
    }

    public void enterBirthYear(String year) {
        birthYearInput.clear();
        birthYearInput.sendKeys(year);
    }

    public String getBirthMonthValue() {
        return birthMonthInput.getAttribute("value");
    }

    public String getBirthDayValue() {
        return birthDayInput.getAttribute("value");
    }

    public String getBirthYearValue() {
        return birthYearInput.getAttribute("value");
    }

    public void enterSSN(String ssn) {
        ssnInput.clear();
        ssnInput.sendKeys(ssn);
    }

    public String getSsnValue() {
        return ssnInput.getAttribute("value");
    }

    public boolean hasSSNError() {
        return !driver.findElements(By.cssSelector("input[name='ssn'] ~ p.text--error")).isEmpty();
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

    public void enterMoveToMNMonth(String month) {
        WebElement input = driver.findElement(By.cssSelector("input[name='moveToMNMonth'"));
        input.clear();
        input.sendKeys(month);
    }

    public void enterMoveToMNDay(String day) {
        WebElement input = driver.findElement(By.cssSelector("input[name='moveToMNDay'"));
        input.clear();
        input.sendKeys(day);
    }

    public void enterMoveToMNYear(String year) {
        WebElement input = driver.findElement(By.cssSelector("input[name='moveToMNYear'"));
        input.clear();
        input.sendKeys(year);
    }

    public boolean displaysAllMoveToMNInputs() {
        return driver.findElements(By.cssSelector("input[name^='moveToMN']")).stream().allMatch(WebElement::isDisplayed);
    }

    public boolean displaysNoMoveToMNInputs() {
        return driver.findElements(By.cssSelector("input[name^='moveToMN']")).stream().noneMatch(WebElement::isDisplayed);
    }

    public String getMoveToMNMonth() {
        return driver.findElements(By.cssSelector("input[name='moveToMNMonth'")).stream().findFirst()
        .map(input -> input.getAttribute("value"))
        .orElse(null);
    }

    public String getMoveToMNDay() {
        return driver.findElements(By.cssSelector("input[name='moveToMNDay'")).stream().findFirst()
        .map(input -> input.getAttribute("value"))
        .orElse(null);
    }

    public String getMoveToMNYear() {
        return driver.findElements(By.cssSelector("input[name='moveToMNYear'")).stream().findFirst()
        .map(input -> input.getAttribute("value"))
        .orElse(null);
    }

    public void enterPreviousCity(String city) {
        WebElement input = driver.findElement(By.cssSelector("input[name='moveToMNPreviousCity'"));
        input.clear();
        input.sendKeys(city);
    }

    public String getPreviousCity() {
        return driver.findElements(By.cssSelector("input[name='moveToMNPreviousCity'")).stream().findFirst()
                .map(input -> input.getAttribute("value"))
                .orElse(null);
    }
}
