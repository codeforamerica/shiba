package org.codeforamerica.shiba.testutilities;

import io.percy.selenium.Percy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class Page {
    protected final RemoteWebDriver driver;
    protected final Percy percy;


    public String getTitle() {
        return driver.getTitle();
    }

    public Page(RemoteWebDriver driver) {
        this.driver = driver;
        this.percy = new Percy(driver);
    }

    private void checkForBadMessageKeys() {
        assertThat(getTitle()).doesNotContain("??");
        assertThat(driver.findElementByXPath("/html").getText()).doesNotContain("??");
    }

    public String getHeader() {
        return driver.findElement(By.tagName("h1")).getText();
    }

    public void goBack() {
        driver.findElement(By.partialLinkText("Go Back")).click();
    }

    public void clickLink(String linkText) {
        percy.snapshot(driver.getTitle());
        checkForBadMessageKeys();
        driver.findElement(By.linkText(linkText)).click();
    }

    public void clickButton(String buttonText) {
        percy.snapshot(driver.getTitle());
        checkForBadMessageKeys();
        WebElement buttonToClick = driver.findElements(By.className("button")).stream()
                .filter(button -> button.getText().contains(buttonText))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No button found containing text: " + buttonText));
        buttonToClick.click();
    }

    public void clickButtonLink(String buttonLinkText) {
        percy.snapshot(driver.getTitle());
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
        List<WebElement> formInputElements = driver.findElements(By.name(inputName + "[]"));
        WebElement firstElement = formInputElements.get(0);
        FormInputHtmlTag formInputHtmlTag = FormInputHtmlTag.valueOf(firstElement.getTagName());
        switch (formInputHtmlTag) {
            case select -> selectFromDropdown(firstElement, value);
            case button -> choose(formInputElements, value);
            case textarea -> enterInput(firstElement, value);
            case input -> {
                switch (InputTypeHtmlAttribute.valueOf(firstElement.getAttribute("type"))) {
                    case text -> {
                        if (firstElement.getAttribute("class").contains("dob-input")) {
                            enterDateInput(inputName, value);
                        } else {
                            enterInput(firstElement, value);
                        }
                    }
                    case number, tel -> enterInput(firstElement, value);
                    case radio, checkbox -> selectEnumeratedInput(formInputElements, value);
                }
            }
            default -> throw new IllegalArgumentException("Cannot find element");
        }
    }

    public void enter(String inputName, List<String> value) {
        checkForBadMessageKeys();
        List<WebElement> formInputElements = driver.findElements(By.name(inputName + "[]"));
        WebElement firstElement = formInputElements.get(0);
        FormInputHtmlTag formInputHtmlTag = FormInputHtmlTag.valueOf(firstElement.getTagName());
        if (formInputHtmlTag == FormInputHtmlTag.input) {
            if (InputTypeHtmlAttribute.valueOf(firstElement.getAttribute("type")) == InputTypeHtmlAttribute.checkbox) {
                selectEnumeratedInput(formInputElements, value);
            } else {
                throw new IllegalArgumentException("Can't select multiple options for non-checkbox inputs");
            }
        } else {
            throw new IllegalArgumentException("Cannot find element");
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

    private void enterInput(WebElement webElement, String input) {
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
        WebElement input = driver.findElement(By.cssSelector(String.format("input[name='%s[]']:nth-of-type(%s)", inputName, datePart.getPosition())));
        input.clear();
        input.sendKeys(value);
    }

    private void selectEnumeratedInput(List<WebElement> webElements, String optionText) {
        WebElement inputToSelect = webElements.stream()
                .map(input -> input.findElement(By.xpath("./..")))
                .filter(label -> label.getText().contains(optionText))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Cannot find value \"%s\"", optionText)));
        inputToSelect.click();
    }

    private void selectEnumeratedInput(List<WebElement> webElements, List<String> options) {
        options.forEach(option -> selectEnumeratedInput(webElements, option));
    }

    private void choose(List<WebElement> yesNoButtons, String value) {
        WebElement buttonToClick = yesNoButtons.stream()
                .filter(button -> button.getText().contains(value))
                .findFirst()
                .orElseThrow();
        buttonToClick.click();
    }

    public void selectFromDropdown(String inputName, String optionText) {
        selectFromDropdown(driver.findElement(By.cssSelector(String.format("select[name='%s']", inputName))), optionText);
    }

    private void selectFromDropdown(WebElement webElement, String optionText) {
        WebElement optionToSelect = webElement
                .findElements(By.tagName("option")).stream()
                .filter(option -> option.getText().equals(optionText))
                .findFirst()
                .orElseThrow();
        optionToSelect.click();
    }

    public WebElement getSelectedOption(String elementId) {
        return driver.findElementById(elementId)
                .findElements(By.tagName("option")).stream()
                .filter(WebElement::isSelected)
                .findFirst()
                .orElseThrow();
    }

    public String getInputValue(String inputName) {
        return driver.findElement(By.cssSelector(String.format("input[name='%s[]']", inputName))).getAttribute("value");
    }

    public String getBirthDateValue(String inputName, DatePart datePart) {
        return driver.findElement(
                By.cssSelector(String.format("input[name='%s[]']:nth-of-type(%s)", inputName, datePart.getPosition()))).getAttribute("value");
    }

    public String getRadioValue(String inputName) {
        return driver.findElements(By.cssSelector(String.format("input[name='%s[]']", inputName))).stream()
                .filter(WebElement::isSelected)
                .map(input -> input.findElement(By.xpath("./..")).getText())
                .findFirst()
                .orElse(null);
    }

    public List<String> getCheckboxValues(String inputName) {
        return driver.findElements(By.cssSelector(String.format("input[name='%s[]']", inputName))).stream()
                .filter(WebElement::isSelected)
                .map(input -> input.findElement(By.xpath("./..")).getText().split("\n")[0])
                .collect(Collectors.toList());
    }

    public String getSelectValue(String inputName) {
        return driver.findElement(By.cssSelector(String.format("select[name='%s[]']", inputName)))
                .findElements(By.tagName("option")).stream()
                .filter(WebElement::isSelected)
                .findFirst()
                .map(WebElement::getText)
                .orElseThrow();
    }

    public boolean hasInputError(String inputName) {
        return !driver.findElements(By.cssSelector(String.format("input[name='%s[]'] ~ p.text--error", inputName))).isEmpty();
    }

    public boolean hasErrorText(String errorMessage) {
        return driver.findElements(By.cssSelector("p.text--error > span"))
                .stream().anyMatch(webElement -> webElement.getText().equals(errorMessage));
    }

    public String findElementTextById(String id) {
        return driver.findElement(By.id(id)).getText();
    }

    public WebElement findElementById(String id) {
        return driver.findElementById(id);
    }

    public void clickElementById(String id) {
        WebElement inputToSelect = driver.findElementById(id);
        inputToSelect.click();
    }
}
