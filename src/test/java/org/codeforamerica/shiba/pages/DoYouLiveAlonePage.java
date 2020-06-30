package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class DoYouLiveAlonePage extends BasePage {
    @FindBy(css = ".button")
    private List<WebElement> yesNoButtons;

    public DoYouLiveAlonePage(RemoteWebDriver driver) {
        super(driver);
    }

    public ExpeditedIncomePage choose(YesNoAnswer yesNoAnswer) {
        WebElement radioToSelect = yesNoButtons.stream()
                .filter(label -> label.getText().contains(yesNoAnswer.getDisplayValue()))
                .findFirst()
                .orElseThrow();
        radioToSelect.click();
        return new ExpeditedIncomePage(driver);
    }
}
