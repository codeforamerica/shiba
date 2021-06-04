package org.codeforamerica.shiba.pages.journeys;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeReporter;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.pages.AccessibilityTestPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AccessibilityJourneyPageTest extends JourneyTest {
    protected static List<Rule> resultsList = new ArrayList<>();
    protected static Results results;
    protected AccessibilityTestPage testPage;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        testPage = new AccessibilityTestPage(driver);
    }

    @AfterAll
    static void tearDownAll() {
        generateAccessibilityReport(results);
    }

    @AfterEach
    void afterEach() {
        resultsList.addAll(testPage.resultsList);
    }

    public static void generateAccessibilityReport(Results results) {
        results.setViolations(resultsList);
        AxeReporter.writeResultsToJsonFile("src/test/resources/accessibility-test-results/testAccessibility", results);
        File jsonFile = new File("src/test/resources/accessibility-test-results/testAccessibility2.json");
        log.info("Found " + results.getViolations().size() + " accessibility related issues.");
    }

}
