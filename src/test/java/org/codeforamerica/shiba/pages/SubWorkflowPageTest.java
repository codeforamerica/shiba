package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.codeforamerica.shiba.pages.events.SubworkflowIterationDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-sub-workflow.yaml"})
@ActiveProfiles("test")
public class SubWorkflowPageTest extends AbstractExistingStartTimePageTest {

    @MockBean
    PageEventPublisher pageEventPublisher;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("start-page-title", Locale.ENGLISH, "start-page-title");
        staticMessageSource.addMessage("end-page-title", Locale.ENGLISH, "end-page-title");
        staticMessageSource.addMessage("solo-page-title", Locale.ENGLISH, "solo-page-title");
        staticMessageSource.addMessage("warning-page-header", Locale.ENGLISH, "This is a warning for: {0}");
    }

    @Test
    void shouldDisplayInputFromSubflowInFinalPage() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
    }

    @Test
    void shouldSupportSkippableFirstPage() {
        navigateTo("startPage");
        testPage.enter("foo", "someinput");
        testPage.clickContinue();
        testPage.enter("inputSkippable", "bar");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
    }

    @Test
    void shouldSupportSoloPageSubworkflow() {
        navigateTo("soloPage");
        testPage.clickContinue();
        verify(pageEventPublisher).publish(any(SubworkflowCompletedEvent.class));
    }

    @Test
    void shouldCompleteSubflowInAnyOfTheConfiguredCompletePages() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 3");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldPublishSubflowCompletedEventAnyOfTheConfiguredCompletePages() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 3");
        testPage.clickContinue();

        verify(pageEventPublisher).publish(any(SubworkflowCompletedEvent.class));
    }

    @Test
    void shouldNotDisplayIterationInEndPageIfIterationWasNotCompleted() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.goBack();
        testPage.goBack();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
    }

    @Test
    void shouldNotDisplayDataFromPastIterationsWhenStartingANewSubworkflow() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        testPage.clickContinue();

        assertThat(testPage.getInputValue("input1")).isEmpty();
    }

    @Test
    void shouldDisplayInputFromAllCompletedIterations() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 3");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToSecondPage");
        assertThat(driver.findElement(By.id("iteration1")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldShowDeleteWarningPage() {
        String warningPageTitle = "warning page title";
        staticMessageSource.addMessage("some-warning-title", Locale.ENGLISH, warningPageTitle);

        String firstIterationInput1Value = "goToSecondPage";
        String secondIterationInput1Value = "goToThirdPage";

        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", firstIterationInput1Value);
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        testPage.clickContinue();
        testPage.enter("input1", secondIterationInput1Value);
        testPage.clickContinue();
        testPage.enter("input3", "text 4");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0-delete"))).isNotNull();
        assertThat(driver.findElement(By.id("iteration1-delete"))).isNotNull();
        assertThat(driver.findElements(By.id("iteration2-delete"))).isEmpty();

        driver.findElement(By.id("iteration1-delete")).click();

        assertThat(testPage.getTitle()).isEqualTo(warningPageTitle);
        assertThat(testPage.findElementTextByName("warning-message")).isEqualTo("This is a warning for: " + secondIterationInput1Value);
        testPage.clickButton("Yes, remove it");

        assertThat(driver.findElement(By.id("iteration0-delete"))).isNotNull();
        assertThat(driver.findElements(By.id("iteration1-delete"))).isEmpty();

        driver.findElement(By.id("iteration0-delete")).click();

        assertThat(testPage.getTitle()).isEqualTo(warningPageTitle);
        assertThat(testPage.findElementTextByName("warning-message")).isEqualTo("This is a warning for: " + firstIterationInput1Value);
        testPage.clickButton("Yes, remove it");

        assertThat(driver.findElements(By.id("iteration0-delete"))).isEmpty();
    }


    @Test
    void shouldPublishSubflowIterationDeleted() {
        String warningPageTitle = "warning page title";
        staticMessageSource.addMessage("some-warning-title", Locale.ENGLISH, warningPageTitle);

        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "text 4");
        testPage.clickContinue();

        driver.findElement(By.id("iteration0-delete")).click();
        testPage.clickButton("Yes, remove it");
        verify(pageEventPublisher).publish(any(SubworkflowIterationDeletedEvent.class));
    }

    @Test
    void shouldGoToSpecifiedPageWhenGoBackFromEndOfTheWorkflow() {
        String redirectPageTitle = "some title";
        staticMessageSource.addMessage("some-redirect-title", Locale.ENGLISH, redirectPageTitle);

        navigateTo("startPage");
        testPage.clickContinue();
        takeSnapShot("test.png");
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        testPage.goBack();

        assertThat(testPage.getTitle()).isEqualTo(redirectPageTitle);
    }

    @Test
    void shouldGoToSpecifiedPageWhenAttemptToDeleteAnyDataEntry() {
        String warningPageTitle = "some title";
        staticMessageSource.addMessage("some-warning-title", Locale.ENGLISH, warningPageTitle);
        String endPageTitle = "some other title";
        staticMessageSource.addMessage("some-other-title", Locale.ENGLISH, endPageTitle);
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        driver.findElement(By.id("iteration0-delete")).click();
        assertThat(driver.getTitle()).isEqualTo(warningPageTitle);
    }

    @Test
    void shouldClearOutSubworkflowsWhenChoosingToRestartSubworkflow() {
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();
        testPage.goBack();

        driver.findElement(By.tagName("button")).click();

        testPage.clickContinue();
        testPage.enter("input1", "goToThirdPage");
        testPage.clickContinue();
        testPage.enter("input3", "new text 2");
        testPage.clickContinue();

        assertThat(driver.findElement(By.id("iteration0")).getText()).isEqualTo("goToThirdPage");
    }

    @Test
    void shouldRedirectWhenPageDoesntHaveNecessaryDatasources() {
        staticMessageSource.addMessage("earlier-page-title", Locale.ENGLISH, "earlierPage");
        navigateTo("startPage");
        testPage.clickContinue();
        testPage.enter("input1", "goToSecondPage");
        testPage.clickContinue();
        testPage.enter("input2", "text 2");
        testPage.clickContinue();

        driver.findElement(By.id("iteration0-delete")).click();

        driver.findElement(By.tagName("button")).click();
        testPage.goBack();
        assertThat(testPage.getTitle()).isEqualTo("earlierPage");
    }
}
