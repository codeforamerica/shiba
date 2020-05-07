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

    public PrepareToApplyPage goBack() {
        backButton.click();

        return new PrepareToApplyPage(driver);
    }

    public void selectWrittenLanguage(String language) {

    }

    public void selectNeedInterpereter() {

    }

    public void clickPrimaryButton() {

    }
}
