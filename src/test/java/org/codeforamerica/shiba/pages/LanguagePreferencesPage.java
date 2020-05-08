package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LanguagePreferencesPage extends BasePage {
    @FindBy(css = "select")
    private List<WebElement> selects;

    @FindBy(partialLinkText = "Go Back")
    private WebElement backButton;

    @FindBy(css = "input[type='radio']")
    private List<WebElement> needInterpreterRadios;

    public LanguagePreferencesPage(RemoteWebDriver driver) {
        super(driver);
    }

    public void selectSpokenLanguage(String language) {
        WebElement optionToSelect = selects.get(0).findElements(By.tagName("option")).stream()
                .filter(option -> option.getText().equals(language))
                .findFirst()
                .orElseThrow();
        optionToSelect.click();
    }

    public String getSelectedSpokenLanguage() {
        return selects.get(0).findElements(By.tagName("option")).stream()
                .filter(WebElement::isSelected)
                .findFirst()
                .map(WebElement::getText)
                .orElseThrow();
    }

    public void selectWrittenLanguage(String language) {
        WebElement optionToSelect = selects.get(1).findElements(By.tagName("option")).stream()
                .filter(option -> option.getText().equals(language))
                .findFirst()
                .orElseThrow();
        optionToSelect.click();
    }

    public String getSelectedWrittenLanguage() {
        return selects.get(1).findElements(By.tagName("option")).stream()
                .filter(WebElement::isSelected)
                .findFirst()
                .map(WebElement::getText)
                .orElseThrow();
    }

    public void selectNeedInterpereter(String needInterpreter) {
        WebElement radioToSelect = needInterpreterRadios.stream()
                .filter(input -> input.findElement(By.xpath("./..")).getText().equals(needInterpreter))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();
    }

    public String getNeedInterpreterSelection() {
        return needInterpreterRadios.stream()
                .filter(WebElement::isSelected)
                .findFirst()
                .map(webElement -> webElement.findElement(By.xpath("./..")))
                .map(WebElement::getText)
                .orElseThrow();
    }

    public ChooseProgramsPage submitUsingPrimaryButton() {
        primaryButton.submit();

        return new ChooseProgramsPage(driver);
    }
}
