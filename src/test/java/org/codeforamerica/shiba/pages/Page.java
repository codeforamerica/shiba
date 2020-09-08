package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.stream.Collectors;

public class Page {
    protected final RemoteWebDriver driver;

    public String getTitle() {
        return driver.getTitle();
    }

    public Page(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public Page goBack() {
        driver.findElement(By.partialLinkText("Go Back")).click();
        return this;
    }

    public void enterInput(String inputName, String input) {
        WebElement webElement = driver.findElement(By.cssSelector(String.format("input[name^='%s']", inputName)));
        webElement.clear();
        webElement.sendKeys(input);
    }

    public void enterDateInput(String inputName, DatePart datePart, String value) {
        WebElement input = driver.findElement(By.cssSelector(String.format("input[name^='%s']:nth-of-type(%s)", inputName, datePart.getPosition())));
        input.clear();
        input.sendKeys(value);
    }

    public void selectEnumeratedInput(String inputName, String optionText) {
        WebElement inputToSelect = driver.findElements(By.cssSelector(String.format("input[name^='%s']", inputName))).stream()
                .map(input -> input.findElement(By.xpath("./..")))
                .filter(label -> label.getText().equals(optionText))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find value \"%s\" or input \"%s\"", optionText, inputName)));
        inputToSelect.click();
    }

    public Page clickPrimaryButton() {
        driver.findElement(By.className("button--primary")).click();
        return this;
    }

    public Page choose(YesNoAnswer yesNoAnswer) {
        List<WebElement> yesNoButtons = driver.findElements(By.className("button"));
        WebElement buttonToClick = yesNoButtons.stream()
                .filter(button -> button.getText().contains(yesNoAnswer.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        buttonToClick.click();
        return this;
    }

    public void selectFromDropdown(String inputName, String optionText) {
        WebElement optionToSelect = driver.findElement(By.cssSelector(String.format("select[name^='%s']", inputName)))
                .findElements(By.tagName("option")).stream()
                .filter(option -> option.getText().equals(optionText))
                .findFirst()
                .orElseThrow();
        optionToSelect.click();
    }

    public String getInputValue(String inputName) {
        return driver.findElement(By.cssSelector(String.format("input[name^='%s']", inputName))).getAttribute("value");
    }

    public String getBirthDateValue(String inputName, DatePart datePart) {
        return driver.findElement(
                By.cssSelector(String.format("input[name^='%s']:nth-of-type(%s)", inputName, datePart.getPosition()))).getAttribute("value");
    }

    public String getRadioValue(String inputName) {
        return driver.findElements(By.cssSelector(String.format("input[name^='%s']", inputName))).stream()
                .filter(WebElement::isSelected)
                .map(input -> input.findElement(By.xpath("./..")).getText())
                .findFirst()
                .orElse(null);
    }

    public List<String> getCheckboxValues(String inputName) {
        return driver.findElements(By.cssSelector(String.format("input[name^='%s']", inputName))).stream()
                .filter(WebElement::isSelected)
                .map(input -> input.findElement(By.xpath("./..")).getText())
                .collect(Collectors.toList());
    }

    public String getSelectValue(String inputName) {
        return driver.findElement(By.cssSelector(String.format("select[name^='%s']", inputName)))
                .findElements(By.tagName("option")).stream()
                .filter(WebElement::isSelected)
                .findFirst()
                .map(WebElement::getText)
                .orElseThrow();
    }

    public WebElement getInputError(String inputName) {
        return driver.findElement(By.cssSelector(String.format("input[name^='%s'] ~ p.text--error", inputName)));
    }

    public boolean hasInputError(String inputName) {
        return !driver.findElements(By.cssSelector(String.format("input[name^='%s'] ~ p.text--error", inputName))).isEmpty();
    }

    public String findElementTextByName(String name) {
        return driver.findElement(By.id(name)).getText();
    }
}
