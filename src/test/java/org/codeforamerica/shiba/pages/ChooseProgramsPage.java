package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

public class ChooseProgramsPage extends BasePage {
    @FindBy(css = ".checkbox")
    private List<WebElement> programOptions;

    public ChooseProgramsPage(RemoteWebDriver driver) {
        super(driver);
    }

    @Override
    public BasePage goBack() {
        backButton.click();

        return new LanguagePreferencesPage(driver);
    }

    public void chooseProgram(String program) {
        WebElement optionToChoose = programOptions.stream()
                .filter(option -> option.findElement(By.tagName("span")).getText().equals(program))
                .findFirst().orElseThrow();

        optionToChoose.click();
    }

    public TestFinalPage clickContinue() {
        primaryButton.click();
        return new TestFinalPage(driver);
    }

    public List<String> selectedPrograms() {
        return this.programOptions.stream()
                .filter(webElement -> webElement.findElement(By.tagName("input")).isSelected())
                .map(webElement -> webElement.findElement(By.tagName("span")).getText())
                .collect(Collectors.toList());
    }

    public boolean hasError() {
        return !driver.findElements(By.cssSelector("p.text--error")).isEmpty();
    }
}
