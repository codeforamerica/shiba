package org.codeforamerica.shiba.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

public abstract class IntermediaryPage<PREVIOUS_PAGE, NEXT_PAGE> extends BasePage {
    private final PREVIOUS_PAGE previousPage;

    @FindBy(partialLinkText = "Go Back")
    protected WebElement backButton;

    @FindBy(css = ".button--primary")
    protected WebElement primaryButton;

    public IntermediaryPage(PREVIOUS_PAGE previousPage, RemoteWebDriver driver) {
        super(driver);
        this.previousPage = previousPage;
    }

    public PREVIOUS_PAGE goBack() {
        backButton.click();

        return this.previousPage;
    }

    public NEXT_PAGE clickPrimaryButton() {
        primaryButton.click();
        return getNextPage();
    }

    protected abstract NEXT_PAGE getNextPage();
}
