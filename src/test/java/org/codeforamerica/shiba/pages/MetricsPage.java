package org.codeforamerica.shiba.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;


public class MetricsPage extends Page {
    @FindBy(css = ".statistic-card")
    List<WebElement> statisticCards;

    public MetricsPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public String getCardValue(String title) {
        return statisticCards.stream()
                .filter(card -> card.getText().contains(title))
                .findFirst()
                .map(element -> element.findElement(By.className("statistic-card__number")).getText())
                .orElse("");
    }
}
