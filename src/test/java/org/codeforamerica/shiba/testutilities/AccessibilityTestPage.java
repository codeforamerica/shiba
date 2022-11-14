package org.codeforamerica.shiba.testutilities;

import com.deque.html.axecore.args.AxeRunOptions;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.openqa.selenium.remote.RemoteWebDriver;

public class AccessibilityTestPage extends Page {

  // Using a map lets us overwrite results from previous scans of a given page
  @Getter
  public Map<String, List<Rule>> resultMap = new HashMap<>();

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

  public void testAccessibility() {
    AxeRunOptions options = new AxeRunOptions();
    options.setResultTypes(List.of("violations"));
    options.setIFrames(false);

    AxeBuilder builder = new AxeBuilder()
        .withOptions(options)
        .disableIframeTesting()
        .withTags(List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "section508"));

    Results results = builder.analyze(driver);
    List<Rule> violations = results.getViolations();
    violations.forEach(rule -> rule.setUrl(getTitle()));
    resultMap.put(getTitle(), violations);
    System.out.println("Testing a11y on page " + getTitle());
  }
}
