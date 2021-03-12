package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class Page {
    protected final RemoteWebDriver driver;

    public String getTitle() {
        return driver.getTitle();
    }

    public Page(RemoteWebDriver driver) {
        this.driver = driver;
    }

    private void checkForBadMessageKeys() {
        assertThat(getTitle()).doesNotContain("??");
        assertThat(driver.findElementByXPath("/html").getText()).doesNotContain("??");
    }

    public void goBack() {
        driver.findElement(By.partialLinkText("Go Back")).click();
    }

    public void clickLink(String linkText) {
        checkForBadMessageKeys();
        driver.findElement(By.linkText(linkText)).click();
    }

    public void clickButton(String buttonText) {
        checkForBadMessageKeys();
        WebElement buttonToClick = driver.findElements(By.className("button")).stream()
                .filter(button -> button.getText().contains(buttonText))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No button found containing text: " + buttonText));
        buttonToClick.click();
    }

    public void clickButtonLink(String buttonLinkText) {
        checkForBadMessageKeys();
        WebElement buttonToClick = driver.findElements(By.className("button--link")).stream()
                .filter(button -> button.getText().contains(buttonLinkText))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No button link found containing text: " + buttonLinkText));
        buttonToClick.click();
    }

    public void clickContinue() {
        clickButton("Continue");
    }

    public void enter(String inputName, String value) {
        checkForBadMessageKeys();
        WebElement formInputElement = driver.findElement(By.cssSelector(String.format("[name^='%s']", inputName)));
        FormInputHtmlTag formInputHtmlTag = FormInputHtmlTag.valueOf(formInputElement.getTagName());
        switch (formInputHtmlTag) {
            case select -> selectFromDropdown(inputName, value);
            case button -> choose(value);
            case textarea -> enterInput(inputName, value);
            case input -> {
                switch (InputTypeHtmlAttribute.valueOf(formInputElement.getAttribute("type"))) {
                    case text -> {
                        if (formInputElement.getAttribute("class").contains("dob-input")) {
                            enterDateInput(inputName, value);
                        } else {
                            enterInput(inputName, value);
                        }
                    }
                    case number, tel -> enterInput(inputName, value);
                    case radio, checkbox -> selectEnumeratedInput(inputName, value);
                }
            }
        }
    }

    enum FormInputHtmlTag {
        input,
        textarea,
        select,
        button
    }

    enum InputTypeHtmlAttribute {
        text,
        number,
        radio,
        checkbox,
        tel
    }

    private void enterInput(String inputName, String input) {
        WebElement webElement = driver.findElement(By.cssSelector(String.format("input[name^='%s']", inputName)));
        webElement.clear();
        webElement.sendKeys(input);
    }

    private void enterDateInput(String inputName, String value) {
        String[] dateParts = value.split("/", 3);
        enterDateInput(inputName, DatePart.MONTH, dateParts[DatePart.MONTH.getPosition() - 1]);
        enterDateInput(inputName, DatePart.DAY, dateParts[DatePart.DAY.getPosition() - 1]);
        enterDateInput(inputName, DatePart.YEAR, dateParts[DatePart.YEAR.getPosition() - 1]);
    }

    public void enterDateInput(String inputName, DatePart datePart, String value) {
        WebElement input = driver.findElement(By.cssSelector(String.format("input[name^='%s']:nth-of-type(%s)", inputName, datePart.getPosition())));
        input.clear();
        input.sendKeys(value);
    }

    private void selectEnumeratedInput(String inputName, String optionText) {
        WebElement inputToSelect = driver.findElements(By.cssSelector(String.format("input[name^='%s']", inputName))).stream()
                .map(input -> input.findElement(By.xpath("./..")))
                .filter(label -> label.getText().contains(optionText))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find value \"%s\" or input \"%s\"", optionText, inputName)));
        inputToSelect.click();
    }

    private void choose(String value) {
        List<WebElement> yesNoButtons = driver.findElements(By.className("button"));
        WebElement buttonToClick = yesNoButtons.stream()
                .filter(button -> button.getText().contains(value))
                .findFirst()
                .orElseThrow();
        buttonToClick.click();
    }

    private void selectFromDropdown(String inputName, String optionText) {
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
                .map(input -> input.findElement(By.xpath("./..")).getText().split("\n")[0])
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

    public void clickElementById(String id) {
        WebElement inputToSelect = driver.findElementById(id);
        inputToSelect.click();
    }
}
