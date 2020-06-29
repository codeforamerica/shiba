package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class DoYouLiveAlonePage extends BasePage {
    @FindBy(css = ".radio-button")
    private List<WebElement> yesNoRadios;

    public DoYouLiveAlonePage(RemoteWebDriver driver) {
        super(driver);
    }

    public ExpeditedIncomePage choose(YesNoAnswer yesNoAnswer) {
        WebElement radioToSelect = yesNoRadios.stream()
                .filter(label -> label.getText().equals(yesNoAnswer.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();
        return new ExpeditedIncomePage(driver);
    }
}
