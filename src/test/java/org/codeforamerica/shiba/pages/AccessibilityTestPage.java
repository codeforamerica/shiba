package org.codeforamerica.shiba.pages;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.List;


public class AccessibilityTestPage extends Page {
    public List<Rule> resultsList = new ArrayList<>();

    public AccessibilityTestPage(RemoteWebDriver driver) {
        super(driver);
    }

    public void clickLink(String linkText) {
        super.clickLink(linkText);
        testAccessibility();
    }

    public void clickButton(String buttonText) {
        super.clickButton(buttonText);
        testAccessibility();
    }

    public void clickButtonLink(String buttonLinkText) {
        super.clickButtonLink(buttonLinkText);
        testAccessibility();
    }

    public void clickContinue() {
        super.clickContinue();
        testAccessibility();
    }

    public void testAccessibility() {
        AxeBuilder builder = new AxeBuilder();
        builder.setOptions("""
                {   "resultTypes": ["violations"],
                    "runOnly": { 
                        "type": "tag", 
                        "values": ["wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "section508"]
                    } 
                }
                """);
        Results results = builder.analyze(driver);
        List<Rule> violations = results.getViolations();
        violations.forEach(rule -> rule.setUrl(getTitle()));
        resultsList.addAll(violations);
    }
}
